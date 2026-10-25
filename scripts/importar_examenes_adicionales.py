"""Segunda ampliación oficial CAR/UV, reproducible sin red.

Usa los PDF, plantillas y transcripciones revisadas de docs/examenes_web/ampliacion_2.
Requiere pdfplumber y Pillow para los recortes originales de Word.
"""
import hashlib
import json
import re
from pathlib import Path

import pdfplumber

ROOT = Path(__file__).resolve().parents[1]
DOC = ROOT / 'docs/examenes_web/ampliacion_2'
BANK = ROOT / 'shared/src/commonMain/composeResources/files'
DATA = json.loads((DOC / 'transcripcion.json').read_text())
KEYS = json.loads((DOC / 'plantillas_origen.json').read_text())
for source in json.loads((DOC / 'fuentes.json').read_text()):
    assert hashlib.sha256((DOC / 'fuentes' / source['archivo']).read_bytes()).hexdigest() == source['sha256']

# Se conserva el número original. En UV cada caso reinicia la numeración.
EXAMS = [
    ('administrativo', 'car_auxiliar_teoria', 'CAR_AUXILIAR_2025_TEORIA', 'CAR 15/11/2025 - auxiliar, teoría seleccionada'),
    ('administrativo', 'uv_teoria', 'UV_2022_TEORIA_CAR', 'UV 14/05/2022 - teoría seleccionada para CAR'),
    ('informatica', 'car_auxiliar_word', 'CAR_AUXILIAR_2025_WORD', 'CAR 15/11/2025 - Word 2016'),
    ('informatica', 'uv_ofimatica_s1', 'UV_2022_OFIMATICA_S1', 'UV 16/07/2022 - Office 2016, independientes del caso 1'),
    ('informatica', 'uv_ofimatica_s2', 'UV_2022_OFIMATICA_S2', 'UV 16/07/2022 - Office 2016, independientes del caso 2'),
    ('informatica', 'uv_ofimatica_s3', 'UV_2022_OFIMATICA_S3', 'UV 16/07/2022 - Office 2016, independientes del caso 3'),
]

CAR_TOPICS = {
    31:'G-I.4',32:'G-I.1',33:'G-I.2',34:'G-I.8',35:'G-I.1',36:'E-II.4',37:'G-I.3',
    38:'G-I.2',39:'G-I.2',40:'G-I.4',41:'G-I.4',42:'G-I.9',43:'G-I.6',44:'G-I.6',
    45:'E-I.3',48:'E-I.3',49:'E-II.1-2',50:'E-II.1',51:'E-II.1',52:'G-II.1-2',
    53:'G-II.1',54:'G-II.2',55:'G-II.2',56:'G-II.3',57:'G-II.3',58:'E-I.10',59:'E-I.10',
    66:'E-I.3',67:'E-I.2',68:'E-I.2',69:'E-I.2',70:'E-I.2',71:'E-I.3 / E-I.8',72:'E-I.3',
    73:'E-I.4',74:'E-I.2 / E-I.4',75:'E-I.4',76:'E-I.1',77:'G-II.4',78:'G-II.4',79:'G-II.6',80:'G-II.6',
}
UV_TOPICS = {
    4:'G-I.10',6:'G-I.8',8:'E-I.3',11:'E-I.6',14:'G-I.1',16:'E-I.3',24:'E-I.6',26:'G-I.5',
    28:'E-I.3',30:'G-I.9',35:'E-I.4',38:'E-I.3',40:'E-I.10',42:'G-I.9',44:'E-I.4',46:'E-I.3',
    47:'G-I.9',48:'G-I.10',52:'E-I.3',54:'E-I.6',55:'G-I.6 / E-III.1',61:'E-I.3',65:'G-I.9',
    68:'E-I.4',71:'G-I.6',74:'E-I.6',76:'E-I.2',80:'E-I.1',81:'E-I.10',82:'G-I.2',88:'E-I.3',
    90:'G-I.1',95:'E-I.2',100:'G-I.10',
}
assert set(UV_TOPICS) == {q['numero'] for q in DATA['uv_teoria']}

# Coordenadas PDF (puntos, origen superior izquierdo), recortes del original.
CROPS = {1:(2,(87,310,125,340)), 8:(3,(72,648,175,744)), 9:(4,(82,100,352,179)),
         13:(5,(75,87,564,204)), 16:(6,(94,94,390,151)),
         20:(7,(75,223,345,282)), 21:(7,(85,448,213,590))}


def normalized(text):
    return ''.join(c for c in text.casefold() if c.isalnum())


def sha256(value):
    return hashlib.sha256(value).hexdigest()


def decision(source, n):
    if KEYS[source][n-1] == 'x':
        return False, 'Anulada por la plantilla modificada oficial.'
    if source == 'car_auxiliar_teoria':
        if n in CAR_TOPICS:
            return True, CAR_TOPICS[n]
        if n <= 30:
            return False, 'Psicotécnicos: fuera del programa CAR FA.13/24-26.'
        if n == 46:
            return False, 'Detalle del libro de Quejas y Sugerencias, fuera del programa.'
        if n == 47:
            return False, 'Sistema de Archivos de La Rioja, fuera del programa.'
        if n in range(60,64):
            return False, 'Legislación específica de igualdad y violencia de género, fuera del programa.'
        return False, 'Régimen específico de transparencia, fuera del programa.'
    if source == 'uv_teoria':
        if n in UV_TOPICS:
            return True, UV_TOPICS[n]
        if n == 62:
            return False, 'Umbral de contratación de 2022; no se traslada como umbral vigente.'
        if n == 64:
            return False, 'Legislación específica de violencia de género, fuera del programa.'
        return False, 'Fuera de la selección CAR: contenido universitario, normativa valenciana o PRL.'
    if source == 'car_auxiliar_word':
        return True, 'INF.3'
    if n not in {q['numero'] for q in DATA[source]}:
        return False, 'No incluida en la selección autónoma revisada; no se importa el supuesto completo.'
    if source == 'uv_ofimatica_s1':
        return True, 'INF.1' if n in {2,5,6,9} else 'INF.3' if n in {11,15} else 'INF.2'
    if source == 'uv_ofimatica_s2':
        return True, 'INF.2' if n == 20 else 'INF.1'
    return True, 'INF.1' if n == 13 else 'INF.2'


# Comparación con todos los bancos CAR previos, excluyendo los que gestiona este
# importador para que ejecutarlo otra vez no descarte sus propias preguntas.
managed = {e[2] for e in EXAMS}
seen = {}
for category in ('administrativo','informatica','supuestos_administrativo_car'):
    for exam in json.loads((BANK/category/'examenes.json').read_text()):
        if exam['id'] in managed:
            continue
        folder = BANK/category/exam['carpeta']
        answers = dict(re.findall(r'(?m)^(\d+)([a-d])$', (folder/exam['respuestas']).read_text()))
        for block in re.split(r'\n(?=\d+\))', (folder/exam['preguntas']).read_text().strip()):
            number = re.match(r'(\d+)\)',block)[1]
            text = re.sub(r'@imagen:.*\n?', '', re.sub(r'^\d+\)', '', block))
            seen[normalized(text)] = (exam['id'], number, answers[number])

audit, fixtures, counts = [], [], {}
for category, source, exam_id, title in EXAMS:
    folder = BANK/category/source
    folder.mkdir(parents=True, exist_ok=True)
    questions = {q['numero']:q for q in DATA[source]}
    blocks, answers = [], []
    for n, letter in enumerate(KEYS[source],1):
        keep, reason = decision(source,n)
        q = questions.get(n)
        text_hash = image_hash = '-'
        if q:
            stem = f"{n}) {q['enunciado']}"
            options = [f'{c}) {v}' for c,v in zip('abcd',q['opciones'],strict=True)]
            block = '\n'.join([stem]+options)
            text_hash = sha256(block.encode())
            fingerprint = normalized(re.sub(r'^\d+\)', '', block))
            if keep and fingerprint in seen:
                previous_id, previous_n, previous_key = seen[fingerprint]
                assert previous_key == letter, (exam_id,n,'duplicado con clave diferente')
                keep, reason = False, f'Duplicado normalizado de {previous_id}, pregunta {previous_n}.'
        if keep:
            assert q is not None and letter in 'abcd', (exam_id,n)
            seen[fingerprint] = (exam_id,n,letter)
            if source == 'car_auxiliar_word' and n in CROPS:
                page, bbox = CROPS[n]
                image_path = folder/'imagenes'/f'pregunta_{n}.png'
                image_path.parent.mkdir(exist_ok=True)
                with pdfplumber.open(DOC/'fuentes/car_aux25_2.pdf') as pdf:
                    pdf.pages[page-1].crop(bbox).to_image(resolution=180).save(image_path)
                image_hash = sha256(image_path.read_bytes())
                block = '\n'.join([stem,f'@imagen: imagenes/pregunta_{n}.png']+options)
            blocks.append(block)
            answers.append(f'{n}{letter}')
        verdict = 'añadir' if keep else 'descartar'
        fixtures.append('\t'.join([exam_id,str(n),verdict,letter,text_hash,image_hash]))
        audit.append(dict(examen=exam_id,origen=source,numero=n,decision=verdict,
                          referencia=reason,respuesta=letter,**({k:v for k,v in q.items() if k!='numero'} if q else {})))
    assert blocks, exam_id
    (folder/'Preguntas.txt').write_text('\n\n'.join(blocks)+'\n')
    (folder/'Respuestas.txt').write_text('\n'.join(answers)+'\n')
    catalog_path = BANK/category/'examenes.json'
    catalog = json.loads(catalog_path.read_text())
    entry = dict(id=exam_id,nombre=title,carpeta=source,preguntas='Preguntas.txt',respuestas='Respuestas.txt')
    if any(e['id']==exam_id for e in catalog):
        catalog = [entry if e['id']==exam_id else e for e in catalog]
    else:
        catalog.append(entry)
    catalog_path.write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n')
    counts[exam_id] = len(blocks)
    print(exam_id,len(blocks))

(DOC/'revision.json').write_text(json.dumps(dict(preguntas=audit,recuento=counts),ensure_ascii=False,indent=2)+'\n')
test_dir = ROOT/'shared/src/jvmTest/resources/examenes_adicionales'
test_dir.mkdir(parents=True,exist_ok=True)
(test_dir/'seleccion.tsv').write_text('# Examen\tNúmero\tDecisión\tClave oficial\tSHA256 texto\tSHA256 imagen\n'+'\n'.join(fixtures)+'\n')
(test_dir/'plantillas.tsv').write_text('# Plantillas completas; UV reinicia los números en cada caso. x = anulada.\n'+'\n'.join(e[2]+'\t'+KEYS[e[1]] for e in EXAMS)+'\n')
print('TOTAL',sum(counts.values()))
