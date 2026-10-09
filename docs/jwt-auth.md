# Autenticación JWT

Genera una clave con `openssl rand -base64 32` y agrega `JWT_SECRET=<clave>`
a `src/main/resources/application-local.properties`. No publiques esa clave. Es necesaria para arrancar la aplicación.
Mantenla estable entre reinicios para conservar la validez de los tokens.

Registro y login reciben el JSON habitual y devuelven un JWT real en `token`.
En Postman usa No Auth para `POST /auth/register` y `POST /auth/login`.
Para `GET /users/me` y las operaciones de tareas, selecciona Bearer Token
y pega el token recibido. No se necesitan cookies ni tokens CSRF.

El token contiene el ID del usuario en `sub`, el emisor, la fecha de emisión
y la fecha de expiración. Está firmado con HS256; no contiene contraseñas.
La validación utiliza Spring Security Resource Server.
Expira según `app.jwt.ttl` (actualmente 10 minutos) desde su emisión, aunque se siga usando.
Un token ausente, inválido o expirado devuelve 401.

No hay endpoint de logout ni revocación: el cliente elimina su token para
cerrar sesión, pero una copia permanece válida hasta expirar.
Refresh tokens y revocación quedan pendientes. Usa HTTPS en producción.
La propiedad de cada tarea debe comprobarse al implementar sus operaciones.
