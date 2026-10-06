# Todo List API

API REST para gestionar tareas personales, desarrollada con Spring Boot y PostgreSQL como proyecto de práctica del reto [Todo List API de roadmap.sh](https://roadmap.sh/projects/todo-list-api).

El objetivo es construir la aplicación paso a paso para practicar desarrollo backend: desde la persistencia y las operaciones CRUD hasta la autenticación y el control de acceso. Cada usuario podrá gestionar sus propias tareas.

## Estado del proyecto

En desarrollo. Por ahora se ha generado el proyecto base y se han agregado las dependencias iniciales. Las funcionalidades descritas a continuación están pendientes de implementación.

## Tecnologías

- Java 17.
- Spring Boot 4.1.1.
- Maven, con Maven Wrapper incluido.
- Spring Web MVC para los endpoints REST.
- Spring Data JPA para la persistencia.
- PostgreSQL como base de datos.
- Bean Validation para validar las peticiones.

La autenticación y la autorización se incorporarán más adelante con Spring Security.

## Funcionalidades previstas

- Registro de usuarios con correo único y almacenamiento seguro de contraseñas mediante hash.
- Inicio de sesión y generación de un token de autenticación.
- Creación, consulta, actualización y eliminación de tareas.
- Acceso a las tareas restringido a su propietario.
- Validación de los datos recibidos.
- Respuestas de error con códigos HTTP adecuados.
- Listado de tareas con paginación y filtros.
- Persistencia de usuarios y tareas en PostgreSQL.

## Endpoints previstos

| Método | Ruta | Propósito | Requiere autenticación |
| --- | --- | --- | --- |
| `POST` | `/register` | Registrar un usuario y obtener un token. | No |
| `POST` | `/login` | Iniciar sesión y obtener un token. | No |
| `POST` | `/todos` | Crear una tarea. | Sí |
| `GET` | `/todos?page=1&limit=10` | Listar las tareas del usuario con paginación. | Sí |
| `PUT` | `/todos/{id}` | Actualizar una tarea del usuario. | Sí |
| `DELETE` | `/todos/{id}` | Eliminar una tarea del usuario. | Sí |

Los filtros y el formato del token se definirán durante la implementación.

## Plan de trabajo

- [x] Crear el proyecto y agregar las dependencias iniciales.
- [ ] Configurar la conexión con PostgreSQL.
- [ ] Modelar las tareas y desarrollar sus operaciones CRUD.
- [ ] Agregar validación y manejo de errores.
- [ ] Modelar los usuarios e implementar registro e inicio de sesión.
- [ ] Proteger los endpoints y comprobar la propiedad de las tareas.
- [ ] Implementar paginación y filtros.
- [ ] Agregar pruebas de las funcionalidades y del control de acceso.
- [ ] Documentar la configuración, la ejecución y ejemplos de uso.

## Mejoras opcionales

Una vez completadas las funcionalidades principales, se podrán incorporar ordenamiento de tareas, límites de peticiones y renovación de tokens.

## Objetivos de aprendizaje

- Organizar una aplicación en controladores, servicios y repositorios.
- Diseñar entidades y relaciones para una base de datos relacional.
- Utilizar DTOs para las peticiones y respuestas de la API.
- Aplicar las convenciones de una API REST y sus códigos HTTP.
- Comprender la diferencia entre autenticación y autorización.
- Verificar el comportamiento de la API mediante pruebas.
