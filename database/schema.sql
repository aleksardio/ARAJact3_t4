CREATE TABLE categorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_categorias_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE materias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    docente VARCHAR(120) NOT NULL,
    semestre INT NOT NULL,
    categoria_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_materias_categoria_id (categoria_id),
    CONSTRAINT chk_materias_semestre CHECK (semestre BETWEEN 1 AND 12),
    CONSTRAINT fk_materias_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
