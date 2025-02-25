# User Service - CalendarUgr

## Descripción
El **User Service** es un microservicio dentro del sistema **CalendarUgr** encargado de la gestión de usuarios y roles. Proporciona funcionalidades para la autenticación, autorización y administración de cuentas de usuario.

## Características
- Registro y gestión de usuarios.
- Asignación y administración de roles.
- Integración con otros microservicios de CalendarUgr.

## Requisitos previos
Para ejecutar este servicio, es necesario configurar las siguientes variables de entorno:

- `DB_USERNAME`: Nombre de usuario de la base de datos.
- `DB_PASSWORD`: Contraseña de la base de datos.
- `DB_URL`: URL de conexión a la base de datos.

## Instalación y ejecución
1. Clonar el repositorio:
   ```sh
   git clone <repository-url>
   cd user-service

2. Configurar variables de entorno 

    ```sh
    export DB_USERNAME=<your_db_username>
    export DB_PASSWORD=<your_db_password>
    export DB_URL=<your_db_url>

3. Construir y ejecutar el servicio

    ```sh
    ./mvnw spring-boot:run