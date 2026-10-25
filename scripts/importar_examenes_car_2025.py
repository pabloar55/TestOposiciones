"""Importación reproducible desde las copias oficiales guardadas en docs.

Requiere pdfplumber, pypdf y Pillow. No descarga ni inventa preguntas o claves.
Ejecutar desde cualquier directorio con el Python que tenga esas dependencias.
"""
import hashlib
import json
import re
from pathlib import Path

import pdfplumber
from PIL import Image
from pypdf import PdfReader

ROOT = Path(__file__).resolve().parents[1]
DOC = ROOT / 'docs/examenes_web/car_2025'
BANK = ROOT / 'shared/src/commonMain/composeResources/files'
SOURCES = DOC / 'fuentes'
for source_file in json.loads((DOC / 'fuentes.json').read_text()):
    assert hashlib.sha256((SOURCES / source_file['archivo']).read_bytes()).hexdigest() == source_file['sha256']
QUESTION = re.compile(r'(?m)^(\d+)\.\s+')
OPTION = re.compile(r'(?m)^([a-d])\)\s*')


def compact(text):
    return ' '.join(text.split())


def digest(text):
    return hashlib.sha256(text.encode()).hexdigest()


def key(text):
    return ''.join(c for c in text.casefold() if c.isalnum())


def clean_page(page, number):
    lines = (page.extract_text(x_tolerance=1.5, y_tolerance=3) or '').splitlines()
    if lines and lines[-1].strip() == str(number):
        lines.pop()
    return '\n'.join(lines)


def read_questions(name, count, contexts=None):
    with pdfplumber.open(SOURCES / f'{name}.pdf') as pdf:
        pages = [clean_page(p, i+1) for i, p in enumerate(pdf.pages)]
    # La portada no contiene preguntas. El contexto del caso se incorpora por
    # separado para que siga disponible al mezclar preguntas aleatoriamente.
    if contexts:
        text = '\n'.join(t for i, t in enumerate(pages, 1) if i not in contexts.values() and i != 1)
    else:
        text = '\n'.join(pages[1:])
    text = text.replace('PREGUNTAS DE RESERVA', '')
    text = re.sub(r'Teniendo en cuenta lo dispuesto en el supuesto práctico [AB], conteste a las siguientes preguntas:', '', text)
    if name == 'car29_1':
        text = re.sub(r'Teniendo en cuenta la tabla “Empleados” que figura a continuación, conteste a las siguientes tres\s+preguntas relacionadas con Access 2016:', '', text)
        text = re.sub(r'(?m)^IMAGEN [123]:\s*', '', text)
    starts = list(QUESTION.finditer(text))
    assert [int(m[1]) for m in starts] == list(range(1, count+1)), name
    result = []
    for i, start in enumerate(starts):
        n = int(start[1])
        block = text[start.end():starts[i+1].start() if i+1 < len(starts) else len(text)]
        if name == 'car28_9':
            block = re.split(r'PREGUNTAS RELACIONADAS CON', block)[0]
        options = list(OPTION.finditer(block))
        assert [m[1] for m in options] == list('abcd'), (name, n)
        stem = compact(block[:options[0].start()])
        choices = [compact(block[m.end():options[j+1].start() if j < 3 else len(block)]) for j, m in enumerate(options)]
        if name == 'car28_9' and n == 11:
            assert choices == ['', '', '', '']
            choices = [f'Consulta {c.upper()} (véase la imagen).' for c in 'abcd']
        assert all(choices), (name, n, choices)
        if name == 'car28_9' and n == 8:
            stem = stem.replace('signo de I.E.S.', 'signo [+] de I.E.S.')
        source_stem = stem
        scenario = None
        if contexts:
            scenario = 'A' if n <= (20 if name == 'car28_2' else 10) else 'B'
            context = compact(pages[contexts[scenario]-1]).removeprefix(f'SUPUESTO PRÁCTICO {scenario}').strip()
            stem += f' [Contexto íntegro del supuesto {scenario}: {context}]'
        source_page = next(i for i, text in enumerate(pages, 1) if re.search(rf'(?m)^{n}\.\s+', text))
        result.append(dict(numero=n, enunciado=stem, enunciado_origen=source_stem, opciones=choices,
                           supuesto=scenario, pagina_pregunta=source_page,
                           pagina_contexto=contexts[scenario] if contexts else None))
    return result


def read_template(name, phases):
    text = '\n'.join(p.extract_text() for p in PdfReader(SOURCES / f'{name}.pdf').pages)
    sections = re.split(r'PRIMERA FASE:|SEGUNDA FASE:', text)[1:] if len(phases) > 1 else [text]
    result = []
    for section, count in zip(sections, phases, strict=True):
        values = {int(n): ('x' if v == 'ANULADA' else v.lower()) for n, v in re.findall(r'(\d+)\.\s*(ANULADA|[A-D])\b', section)}
        assert set(values) == set(range(1, count+1)), (name, values)
        result.append(values)
    return result


T28 = read_template('car28_4', [90, 40])
T29 = read_template('car29_3', [70, 20])
TOFFICE = read_template('car28_13', [20])[0]
DATA = {
    'car28_1': read_questions('car28_1', 90),
    'car28_2': read_questions('car28_2', 40, {'A': 2, 'B': 7}),
    'car28_9': read_questions('car28_9', 20),
    'car29_1': read_questions('car29_1', 70),
    'car29_2': read_questions('car29_2', 20, {'A': 2, 'B': 5}),
}

# Temas explícitos del programa, revisados por rangos y por pregunta.
def ranges(*groups):
    return {n: topic for start, end, topic in groups for n in range(start, end+1)}


TOPICS = {
    'car28_1': ranges((1,3,'G-I.1'), (4,6,'G-I.2'), (7,8,'G-I.3'), (9,11,'G-I.4'),
        (12,12,'G-I.6'), (13,14,'G-I.5'), (15,15,'G-I.6'), (16,17,'G-I.7'), (18,21,'G-I.8'),
        (22,25,'G-I.9'), (26,28,'G-I.10'), (29,36,'G-II.1-3'), (37,39,'G-II.4'),
        (40,42,'G-II.5'), (43,45,'G-II.6'), (46,47,'E-I.1'), (48,49,'E-I.2'), (50,51,'E-I.5'),
        (52,53,'E-I.6'), (54,55,'E-I.7'), (56,57,'E-I.3'), (58,59,'E-I.4'), (60,60,'E-I.8'),
        (61,62,'E-I.9'), (63,64,'E-I.10'), (65,69,'E-II.1-2'), (70,70,'E-II.3'),
        (71,72,'E-II.4'), (73,74,'E-II.5'), (75,79,'E-III.1-2'), (80,82,'E-III.4'),
        (83,84,'E-III.5'), (85,86,'E-III.6'), (87,88,'E-III.7'), (89,90,'E-III.8')),
    'car28_2': ranges((1,3,'G-I.2,4 / E-I.1'), (4,4,'E-I.4'), (5,5,'E-I.1'), (6,7,'E-I.2'),
        (8,10,'E-I.6'), (11,18,'E-III.2-3,5'), (19,20,'G-II.5 / E-I.8'), (21,32,'E-II.1-2'),
        (33,33,'E-III.4'), (34,35,'E-I.3'), (36,40,'E-III.6')),
    'car28_9': ranges((1,7,'INF.3'), (8,14,'INF.1'), (15,20,'INF.2')),
    'car29_1': ranges((1,3,'E-I.5'), (4,6,'E-I.6'), (7,7,'E-I.7'), (8,10,'E-I.8'), (11,13,'E-I.9'),
        (19,24,'E-II.1-2'), (25,28,'E-II.3'), (29,36,'E-III.1-3'), (37,39,'E-III.5'),
        (40,41,'E-III.4'), (42,46,'E-III.6'), (47,51,'E-III.7'), (52,53,'INF.3'),
        (54,57,'INF.2'), (58,60,'INF.1'), (62,63,'E-III.4'), (64,64,'E-I.6'),
        (66,66,'E-III.1'), (67,67,'E-II.1'), (68,68,'E-I.6'), (69,69,'E-III.6'), (70,70,'INF.3')),
    'car29_2': ranges((1,3,'E-I.6 / E-III.5'), (4,7,'E-III.2-3'), (8,10,'E-I.9'),
        (11,14,'E-II.1-2'), (15,17,'E-III.3-4'), (18,20,'E-III.6')),
}
OMIT_PI = {n: 'Fuera del programa: régimen específico de transparencia de la Ley 3/2014.' for n in [14,15,16,17,65]}
OMIT_PI.update({18: 'Fuera de los apartados delimitados de E-I.10: tratamiento específico de videovigilancia.',
                61: 'Fuera de los apartados delimitados de E-I.10: régimen de responsables y encargados.',
                38: 'Umbral de la Ley anual de Presupuestos de 2024; no se traslada como umbral vigente.',
                63: 'Régimen de la Ley anual de Presupuestos de 2024; no se ha validado su traslado al ejercicio actual.'})

# (Categoría, carpeta, identificador, nombre, origen, plantilla, números).
EXAMS = [
    ('administrativo','car_2025_libre_teorico','CAR_2025_LIBRE_TEORICO','CAR 18/10/2025 - libre, teoría','car28_1',T28[0],range(1,91)),
    ('supuestos_administrativo_car','car_2025_libre_supuestos','CAR_2025_LIBRE_SUPUESTOS','CAR 18/10/2025 - libre, supuestos','car28_2',T28[1],range(1,41)),
    ('administrativo','car_2025_pi_teorico','CAR_2025_PI_TEORICO','CAR 23/10/2025 - promoción interna, teoría','car29_1',T29[0],[n for n in range(1,71) if n not in set(range(52,61))|{70}]),
    ('supuestos_administrativo_car','car_2025_pi_supuestos','CAR_2025_PI_SUPUESTOS','CAR 23/10/2025 - promoción interna, supuestos','car29_2',T29[1],range(1,21)),
    ('informatica','car_2025_ofimatica','CAR_2025_OFIMATICA','CAR 13/12/2025 - Word, Access y Excel 2016','car28_9',TOFFICE,range(1,21)),
    ('informatica','car_2025_pi_ofimatica','CAR_2025_PI_OFIMATICA','CAR 23/10/2025 - ofimática, promoción interna','car29_1',T29[0],list(range(52,61))+[70]),
]


def crop(source, page, bbox, dest):
    with pdfplumber.open(SOURCES / f'{source}.pdf') as pdf:
        pdf.pages[page-1].crop(bbox).to_image(resolution=180).save(dest)


# Página y recuadro de material original necesario para contestar. Coordenadas
# PDF en puntos, con origen superior izquierdo. Sin recrear tablas ni diagramas.
CROPS = {
    'car28_9': {
        1:(2,(50,150,555,725)), 2:(2,(50,150,555,725)), 3:(2,(50,150,555,725)),
        6:(4,(50,50,555,220)), 7:(2,(140,530,455,725)),
        8:(5,(55,172,550,460)), 9:(5,(60,588,550,670)),
        11:(6,(50,202,555,790)), 13:(7,(55,205,550,580)),
        15:(8,(50,150,555,445)), 18:(9,(70,95,430,212)),
        19:(9,(70,332,385,410)), 20:(9,(65,555,250,700)),
    },
    'car29_1': {55:(12,(50,45,555,660)), 58:(13,(50,320,360,665)),
                  59:(13,(50,320,345,530)), 60:(13,(50,320,345,530))},
}

managed = {e[1] for e in EXAMS}
seen = {}
for category in ('administrativo','informatica','supuestos_administrativo_car'):
    catalog = json.loads((BANK/category/'examenes.json').read_text())
    for exam in catalog:
        if exam['carpeta'] in managed:
            continue
        folder = BANK/category/exam['carpeta']
        qtext = (folder/exam['preguntas']).read_text()
        answers = {int(n):a for n,a in re.findall(r'(?m)^(\d+)([a-d])$', (folder/exam['respuestas']).read_text())}
        for block in re.split(r'\n(?=\d+\))', qtext.strip()):
            n = int(re.match(r'(\d+)\)',block)[1])
            normalized = key(re.sub(r'@imagen:.*\n?', '', re.sub(r'^\d+\)', '', block)))
            seen[normalized] = (exam['id'], n, answers[n])

audit, counts, fixtures, templates = [], {}, [], []
for category, foldername, exam_id, title, source, template, allowed in EXAMS:
    folder = BANK/category/foldername
    folder.mkdir(parents=True, exist_ok=True)
    qblocks, answers, added = [], [], []
    # Borrar únicamente los recortes que este importador gestiona, en una nueva
    # ejecución. No toca imágenes de los bancos importados anteriormente.
    image_dir = folder/'imagenes'
    image_dir.mkdir(exist_ok=True)
    for old in image_dir.glob('pregunta_*.png'):
        old.unlink()
    for n in allowed:
        q = dict(DATA[source][n-1])
        letter = template[n]
        decision = 'añadir'
        reason = TOPICS[source].get(n, '')
        if letter == 'x': decision, reason = 'descartar', 'Anulada por la plantilla oficial corregida.'
        elif source == 'car29_1' and n in OMIT_PI: decision, reason = 'descartar', OMIT_PI[n]
        stem = f"{n}) {q['enunciado']}"
        choices = [f'{c}) {v}' for c,v in zip('abcd',q['opciones'],strict=True)]
        block = '\n'.join([stem]+choices)
        fingerprint = key(re.sub(r'^\d+\)', '', block))
        if decision == 'añadir' and fingerprint in seen:
            original_id, original_n, original_key = seen[fingerprint]
            assert letter == original_key, (exam_id,n,'duplicado con clave diferente')
            decision, reason = 'descartar', f'Duplicado normalizado de {original_id}, pregunta {original_n}.'
        image_hash = '-'
        if decision == 'añadir':
            assert reason, (source,n)
            seen[fingerprint] = (exam_id,n,letter)
            if n in CROPS.get(source, {}):
                page, bounds = CROPS[source][n]
                image_path = image_dir/f'pregunta_{n}.png'
                crop(source,page,bounds,image_path)
                if source == 'car28_9' and n == 6:
                    # Se necesitan tanto el gráfico del enunciado como los
                    # iconos de las opciones. La app admite una imagen por pregunta.
                    with pdfplumber.open(SOURCES / f'{source}.pdf') as pdf:
                        chart = pdf.pages[1].crop((140,530,455,725)).to_image(resolution=180).original.convert('RGB')
                    icons = Image.open(image_path).convert('RGB')
                    panel = Image.new('RGB', (max(chart.width,icons.width), chart.height+icons.height+20), 'white')
                    panel.paste(chart, (0,0))
                    panel.paste(icons, (0,chart.height+20))
                    panel.save(image_path)
                image_hash = hashlib.sha256(image_path.read_bytes()).hexdigest()
                block = '\n'.join([stem,f'@imagen: imagenes/pregunta_{n}.png']+choices)
            qblocks.append(block)
            answers.append(f'{n}{letter}')
            added.append(n)
        fixtures.append('\t'.join([exam_id,str(n),decision,letter,digest(stem+'\n'+'\n'.join(choices)),image_hash]))
        q.update(examen=exam_id, origen=source, decision=decision, referencia=reason, respuesta=letter)
        audit.append(q)
    assert qblocks, exam_id
    (folder/'Preguntas.txt').write_text('\n\n'.join(qblocks)+'\n')
    (folder/'Respuestas.txt').write_text('\n'.join(answers)+'\n')
    catalog_path = BANK/category/'examenes.json'
    catalog = json.loads(catalog_path.read_text())
    catalog = [e for e in catalog if e['id'] != exam_id]
    catalog.append(dict(id=exam_id,nombre=title,carpeta=foldername,preguntas='Preguntas.txt',respuestas='Respuestas.txt'))
    catalog_path.write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n')
    counts[exam_id] = len(added)
    templates.append(exam_id+'\t'+''.join(template[n] for n in range(1,max(template)+1)))
    print(exam_id, len(added), 'añadidas de', len(allowed))

(DOC/'revision.json').write_text(json.dumps(dict(preguntas=audit,recuento=counts),ensure_ascii=False,indent=2)+'\n')
test_dir = ROOT/'shared/src/jvmTest/resources/examenes_web'
test_dir.mkdir(parents=True,exist_ok=True)
(test_dir/'seleccion.tsv').write_text('# Examen\tNúmero\tDecisión\tClave oficial\tSHA256 texto normalizado\tSHA256 imagen\n'+'\n'.join(fixtures)+'\n')
(test_dir/'plantillas.tsv').write_text('# Plantillas oficiales completas; x = anulada. Los números coinciden con el PDF de origen.\n'+'\n'.join(templates)+'\n')
print('TOTAL', sum(counts.values()))
