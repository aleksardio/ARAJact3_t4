-- Ejecutar manualmente sobre la base local seleccionada, después de schema.sql.
-- Una materia existente con el mismo nombre se conserva sin cambios.
-- Ejecutar una sola carga a la vez: materias no tiene una restricción UNIQUE.
SET NAMES utf8mb4;

START TRANSACTION;

INSERT INTO categorias (nombre, descripcion)
SELECT datos.nombre, NULL
FROM (
    SELECT 'Programación' AS nombre
    UNION ALL SELECT 'Sistemas'
    UNION ALL SELECT 'Ingeniería de Software'
    UNION ALL SELECT 'Redes'
    UNION ALL SELECT 'Bases de Datos'
) AS datos
WHERE NOT EXISTS (
    SELECT 1 FROM categorias existente WHERE existente.nombre = datos.nombre
);

INSERT INTO materias (nombre, docente, semestre, categoria_id)
SELECT datos.nombre, 'Por asignar', datos.semestre, categoria.id
FROM (
    SELECT 'Programación Lógica y Funcional' AS nombre, 8 AS semestre, 'Programación' AS categoria
    UNION ALL SELECT 'Sistemas Programables', 8, 'Sistemas'
    UNION ALL SELECT 'Gestión de Proyectos de Software', 8, 'Ingeniería de Software'
    UNION ALL SELECT 'Programación Web', 8, 'Programación'
    UNION ALL SELECT 'Software para Toma de Decisiones', 8, 'Ingeniería de Software'
    UNION ALL SELECT 'Administración de Redes', 8, 'Redes'
    UNION ALL SELECT 'Taller de Bases de Datos', 5, 'Bases de Datos'
    UNION ALL SELECT 'Fundamentos de Ingeniería de Software', 5, 'Ingeniería de Software'
    UNION ALL SELECT 'Simulación', 5, 'Sistemas'
    UNION ALL SELECT 'Administración de Bases de Datos', 6, 'Bases de Datos'
    UNION ALL SELECT 'Ingeniería de Software', 6, 'Ingeniería de Software'
    UNION ALL SELECT 'Redes de Computadoras', 6, 'Redes'
    UNION ALL SELECT 'Lenguajes de Interfaz', 6, 'Sistemas'
    UNION ALL SELECT 'Conmutación y Enrutamiento de Redes de Datos', 7, 'Redes'
    UNION ALL SELECT 'Fundamentos de Bases de Datos', 4, 'Bases de Datos'
) AS datos
JOIN categorias categoria ON categoria.nombre = datos.categoria
WHERE NOT EXISTS (
    SELECT 1 FROM materias existente WHERE existente.nombre = datos.nombre
);

COMMIT;
