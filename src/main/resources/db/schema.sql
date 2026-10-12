BEGIN;

CREATE TABLE public.users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE public.tasks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    user_id BIGINT NOT NULL,

    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id)
        REFERENCES public.users (id)
);

CREATE INDEX idx_tasks_user_id
    ON public.tasks (user_id);

-- Descripción de la tabla y sus campos.
COMMENT ON TABLE public.users IS
    'Usuarios registrados en la aplicación.';

COMMENT ON COLUMN public.users.id IS
    'Identificador único del usuario, generado automáticamente.';

COMMENT ON COLUMN public.users.username IS
    'Nombre de usuario único utilizado para identificar la cuenta.';

COMMENT ON COLUMN public.users.first_name IS
    'Nombre del usuario.';

COMMENT ON COLUMN public.users.last_name IS
    'Apellido del usuario. Campo opcional.';

COMMENT ON COLUMN public.users.email IS
    'Dirección de correo electrónico única del usuario.';

COMMENT ON COLUMN public.users.password_hash IS
    'Hash de la contraseña del usuario. Nunca contiene la contraseña en texto plano.';

COMMENT ON TABLE public.tasks IS
    'Tareas personales de los usuarios.';

COMMENT ON COLUMN public.tasks.id IS
    'Identificador único de la tarea, generado automáticamente.';

COMMENT ON COLUMN public.tasks.title IS
    'Título que describe brevemente la tarea.';

COMMENT ON COLUMN public.tasks.description IS
    'Descripción detallada de la tarea. Campo opcional.';

COMMENT ON COLUMN public.tasks.user_id IS
    'Identificador del usuario propietario de la tarea. Referencia a users.id.';

COMMIT;
