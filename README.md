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

## Ejecutar

Con Java 17, MySQL local en ejecución y las variables configuradas en PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación estará disponible en `http://localhost:8085`. Hibernate usa `ddl-auto=validate`: no crea ni modifica tablas y exige un esquema compatible con las entidades. Esta etapa todavía no incluye funciones CRUD.

Para comprobar únicamente la compilación sin iniciar el contexto Spring ni conectar a MySQL:

```powershell
.\mvnw.cmd compile
```
