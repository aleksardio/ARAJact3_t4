# Actividad 3

Proyecto Spring Boot con Maven Wrapper, JPA y MySQL. La aplicación utiliza el puerto **8085**.

## Preparar MySQL para desarrollo

1. Instala MySQL 8 en Windows y asegúrate de que el servicio esté ejecutándose localmente en el puerto 3306.
2. Conéctate a MySQL con una cuenta administradora y crea una base y un usuario independientes para desarrollo. Por ejemplo, ejecuta las siguientes sentencias en el cliente MySQL, sustituyendo el marcador por una contraseña local segura:

   ```sql
   CREATE DATABASE araj_act3_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'act3_dev'@'localhost' IDENTIFIED BY 'SUSTITUIR_POR_CONTRASENA_SEGURA';
   GRANT ALL PRIVILEGES ON araj_act3_dev.* TO 'act3_dev'@'localhost';
   ```

   No guardes la contraseña real en archivos versionados ni la subas a GitHub.
3. Define las variables de entorno en PowerShell, en la misma sesión desde la que ejecutarás Maven:

   ```powershell
   $env:DB_URL = 'jdbc:mysql://localhost:3306/araj_act3_dev'
   $env:DB_USERNAME = 'act3_dev'
   $env:DB_PASSWORD = Read-Host 'Contraseña de MySQL para desarrollo'
   ```

   La URL y el usuario tienen esos mismos valores predeterminados; `DB_PASSWORD` debe definirse explícitamente. `Read-Host` muestra el texto escrito en pantalla: usa un entorno privado. Las variables duran solo durante la sesión actual de PowerShell.

`.env.example` muestra los nombres y valores ilustrativos. Un archivo `.env` **no es leído automáticamente por Spring Boot** sin un mecanismo adicional; definir las variables en PowerShell como se indica arriba no requiere uno. Los archivos `.env` y de configuración local privada están ignorados por Git, pero comprueba siempre los cambios antes de publicarlos.

La base local `araj_act3_dev` es independiente de `araj_act3` en el VPS de DigitalOcean. El MySQL del VPS solo escucha en `127.0.0.1:3306` del propio servidor y esta configuración no requiere acceder a él.

## Crear las tablas locales

Con la base local creada, ejecuta manualmente el esquema desde PowerShell, situado en la raíz del proyecto:

```powershell
mysql --host=127.0.0.1 --port=3306 --user=act3_dev --password araj_act3_dev --execute="source database/schema.sql"
```

El cliente solicitará la contraseña sin incluirla en el comando. Si `mysql` no está en el PATH, utiliza la ruta completa a `mysql.exe`. Comprueba que el destino sea la base local antes de ejecutar el comando. El archivo crea `categorias` y `materias`, sin datos de ejemplo; está pensado para una base sin esas tablas y fallará si ya existen. No elimina ni modifica tablas existentes.

## Cargar datos iniciales en MySQL Workbench

El archivo `database/datos-iniciales.sql` contiene 5 categorías y 15 materias de la retícula; usa `Por asignar` como docente provisional. La carga es manual: Spring Boot no ejecuta el archivo al iniciar.

1. Abre en MySQL Workbench la conexión a **MySQL local**, comprueba el servidor y selecciona `araj_act3_dev` como esquema predeterminado (doble clic sobre el esquema). Las tablas deben existir previamente mediante `database/schema.sql`.
2. Abre `database/datos-iniciales.sql` con **File > Open SQL Script**. Confirma de nuevo que el esquema seleccionado sea `araj_act3_dev` y ejecuta todo el script con el botón del rayo correspondiente o **Query > Execute All**. No ejecutes varias cargas simultáneamente.
3. Comprueba los datos en una pestaña SQL del mismo esquema:

   ```sql
   SELECT COUNT(*) AS total_categorias FROM categorias;
   SELECT COUNT(*) AS total_materias FROM materias;

   SELECT m.nombre, m.docente, m.semestre, c.nombre AS categoria
   FROM materias m
   JOIN categorias c ON c.id = m.categoria_id
   ORDER BY m.semestre, m.nombre;
   ```

En una base inicialmente vacía se esperan 5 categorías y 15 materias; si ya había datos, los totales pueden ser mayores. El script reutiliza categorías por nombre y omite cualquier materia cuyo nombre ya exista, aunque sus otros campos sean distintos: no modifica ni elimina esos registros. La comparación sigue la intercalación de las columnas de MySQL. Las ejecuciones sucesivas no duplican estos datos, siempre que se realicen una a la vez; el esquema no impone unicidad en los nombres de materias. Si ocurre un error durante la transacción, ejecuta `ROLLBACK` en la misma sesión y revisa el problema antes de reintentar.

## Ejecutar

Con Java 17, MySQL local en ejecución y las variables configuradas en PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación estará disponible en `http://localhost:8085`. Hibernate usa `ddl-auto=validate`: no crea ni modifica tablas y exige un esquema compatible con las entidades.

Para utilizar temporalmente el puerto 8087 en Windows, sin cambiar la configuración predeterminada:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8087"
```

Para comprobar únicamente la compilación sin iniciar el contexto Spring ni conectar a MySQL:

```powershell
.\mvnw.cmd compile
```

## API REST: pruebas con Postman

Usa `http://localhost:8087` como URL base si iniciaste la aplicación con el argumento anterior (o el puerto configurado de otra forma). Para POST y PUT selecciona **Body > raw > JSON** y envía `Content-Type: application/json`. Los PUT reemplazan todos los campos indicados a continuación; `descripcion` es opcional.

| Método | Ruta | Resultado exitoso |
| --- | --- | --- |
| GET | `/api/categorias` | 200, lista de categorías |
| GET | `/api/categorias/{id}` | 200, categoría |
| POST | `/api/categorias` | 201, categoría creada y cabecera `Location` |
| PUT | `/api/categorias/{id}` | 200, categoría actualizada |
| DELETE | `/api/categorias/{id}` | 204, sin cuerpo; solo si no tiene materias |
| GET | `/api/materias` | 200, lista de materias |
| GET | `/api/materias?categoriaId=1` | 200, materias de la categoría existente |
| GET | `/api/materias/{id}` | 200, materia |
| POST | `/api/materias` | 201, materia creada y cabecera `Location` |
| PUT | `/api/materias/{id}` | 200, materia actualizada |
| DELETE | `/api/materias/{id}` | 204, sin cuerpo |

Primero crea una categoría con `POST /api/categorias`:

```json
{"nombre":"Ciencias","descripcion":"Materias de ciencias"}
```

Respuesta esperada (el ID real puede ser diferente):

```json
{"id":1,"nombre":"Ciencias","descripcion":"Materias de ciencias"}
```

Después usa el ID obtenido como `categoriaId` en `POST /api/materias`:

```json
{"nombre":"Física","docente":"Ana López","semestre":2,"categoriaId":1}
```

Respuesta esperada (los ID reales pueden ser diferentes):

```json
{"id":1,"nombre":"Física","docente":"Ana López","semestre":2,"categoriaId":1,"categoriaNombre":"Ciencias"}
```

Para actualizar, envía el mismo formato de entrada mediante PUT a la ruta del ID correspondiente. Nombres y docente son obligatorios; el semestre debe estar entre 1 y 12 y la categoría debe existir. Para eliminar una categoría, elimina primero sus materias, comprobando que sean datos propios de prueba.

Los errores utilizan un cuerpo como `{"estado":409,"mensaje":"Ya existe una categoría con ese nombre"}`. Datos inválidos o JSON mal formado producen 400; un registro o categoría inexistente produce 404; un conflicto de duplicidad, dependencia o integridad produce 409. No se incluyen detalles internos de SQL. Antes de probar manualmente en Postman, configura MySQL local y las variables descritas arriba; evita modificar o borrar registros ajenos.
