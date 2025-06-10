# TempusUGR - User Service

Este repositorio contiene el código fuente del `user-service`, el microservicio central para la gestión de usuarios dentro del proyecto **TempusUGR**.

Este servicio actúa como la **única fuente de verdad** para toda la información relacionada con los perfiles de usuario, incluyendo sus datos personales, credenciales (almacenadas de forma segura), roles y estado de la cuenta.

---

## ✨ Funcionalidades Principales

* **Gestión de Usuarios (CRUD)**: Proporciona endpoints para crear, leer, actualizar y eliminar usuarios del sistema.
* **Registro y Activación**: Maneja el flujo de registro de nuevos usuarios, incluyendo la generación de un token de activación que se envía por correo electrónico (a través de un evento en RabbitMQ).
* **Gestión de Credenciales**: Almacena las contraseñas de los usuarios de forma segura utilizando un algoritmo de hashing robusto como **BCrypt**. Ofrece funcionalidades para el cambio y reseteo de contraseñas.
* **Gestión de Roles**: Administra los roles del sistema (ej. `ROLE_STUDENT`, `ROLE_TEACHER`, `ROLE_ADMIN`) y los asocia a los usuarios para controlar los permisos de acceso en toda la plataforma.
* **Publicación de Eventos**: Publica mensajes en **RabbitMQ** cuando ocurren eventos importantes, como el registro de un nuevo usuario, para que otros servicios puedan reaccionar de forma asíncrona.

![Image](https://github.com/user-attachments/assets/c8dab51c-65ad-495b-a6cd-d2c727aca1a6)
---

## 🛠️ Pila Tecnológica

* **Lenguaje/Framework**: Java 21, Spring Boot 3.4.4
* **Base de Datos**: **MySQL**, para el almacenamiento relacional de los datos de usuarios, roles y tokens temporales.
* **Seguridad**: **Spring Security** para la gestión de contraseñas (hashing) y la configuración de seguridad de los endpoints.
* **Persistencia de Datos**: **Spring Data JPA** para interactuar con la base de datos MySQL.
* **Mensajería Asíncrona**: **RabbitMQ** para la publicación de eventos.
* **Descubrimiento de Servicios**: Cliente de **Eureka** para el registro en la red de microservicios.

---

## 🏗️ Arquitectura e Interacciones

El `user-service` es un pilar fundamental en la arquitectura y es consumido por varios otros servicios:

* **`auth-service` (REST)**: Le solicita los datos de un usuario (incluido el hash de la contraseña) durante el proceso de inicio de sesión para validar las credenciales.
* **`academic-subscription-service` (REST)**: Puede consultarlo para obtener detalles de un usuario antes de realizar una operación.
* **`mail-service` (Vía RabbitMQ)**: Este servicio publica un mensaje en una cola de RabbitMQ tras un registro exitoso, que es consumido por el `mail-service` para enviar el correo de activación.
* **`api-gateway`**: Todas las peticiones externas hacia este servicio pasan primero por el gateway, que valida el token JWT.
* **`eureka-service`**: Se registra en Eureka para ser descubierto por otros servicios.

---

## 🔌 API Endpoints Principales

El servicio expone endpoints bajo el prefijo `/user`.

| Método | Ruta                      | Descripción                                     |
| :----- | :------------------------ | :---------------------------------------------- |
| `POST` | `/register`               | Registra un nuevo usuario en el sistema.        |
| `POST` | `/activate`               | Activa una cuenta con un token de activación.   |
| `PUT`  | `/password`               | Permite al usuario actual cambiar su contraseña.|
| `PUT`  | `/nickname`               | Permite al usuario actual cambiar su nickname.  |
| `GET`  | `/email/{email}`          | Obtiene la información de un usuario por su email.|
| `GET`  | `/user-info`              | Devuelve la información del usuario autenticado.|
| `POST` | `/admin/register`         | (Admin) Registra un usuario sin activación.     |
| `DELETE`| `/admin/delete/{id}`      | (Admin) Elimina un usuario por su ID.           |
| `POST` | `/role/create`            | (Admin) Crea un nuevo rol en el sistema.        |

---

## 🚀 Puesta en Marcha Local

### **Prerrequisitos**

* Java 21 o superior.
* Maven 3.x.
* Una instancia de **MySQL** en ejecución.
* Una instancia de **RabbitMQ** en ejecución.
* Un servidor **Eureka** (`eureka-service`) en ejecución.

### **Configuración**

Configura los siguientes parámetros en el archivo `src/main/resources/application.properties`:

```properties
# -- CONFIGURACIÓN DEL SERVIDOR --
server.port=8081 # O el puerto deseado

# -- CONFIGURACIÓN DE EUREKA --
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka

# -- CONFIGURACIÓN DE MYSQL --
spring.datasource.url=jdbc:mysql://<host>:<port>/db_user_service?createDatabaseIfNotExist=true
spring.datasource.username=<user>
spring.datasource.password=<password>
spring.jpa.hibernate.ddl-auto=update

# -- CONFIGURACIÓN DE RABBITMQ --
spring.rabbitmq.host=<rabbitmq_host>
spring.rabbitmq.port=5672
spring.rabbitmq.username=<user>
spring.rabbitmq.password=<password>
