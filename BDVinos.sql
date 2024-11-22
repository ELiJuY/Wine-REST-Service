create database Vinos;
use Vinos;
CREATE TABLE Usuarios (
    nombre_de_usuario VARCHAR(100) UNIQUE NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
	PRIMARY KEY (nombre_de_usuario)
) COLLATE=utf8_bin;

CREATE TABLE Seguidores (
    nombre_de_usuario_seguidor VARCHAR(100) NOT NULL,
    nombre_de_usuario_seguido VARCHAR(100) NOT NULL,
    FOREIGN KEY (nombre_de_usuario_seguidor) REFERENCES Usuarios(nombre_de_usuario) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (nombre_de_usuario_seguido) REFERENCES Usuarios(nombre_de_usuario) ON DELETE CASCADE ON UPDATE CASCADE,
	PRIMARY KEY (nombre_de_usuario_seguidor, nombre_de_usuario_seguido)
) COLLATE=utf8_bin;

CREATE TABLE Vinos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre_botella VARCHAR(100) NOT NULL,
    pertenece_a_bodega VARCHAR(100) NOT NULL,
    anada INT NOT NULL,
    tipo_de_vino VARCHAR(100) NOT NULL,
    origen VARCHAR(100) NOT NULL,
    ano INT NOT NULL,
    nombre_de_usuario VARCHAR(100) NOT NULL,
    fecha_adicion DATE NOT NULL,
    puntuacion INT DEFAULT -1,
    FOREIGN KEY (nombre_de_usuario) REFERENCES Usuarios(nombre_de_usuario) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (nombre_botella, pertenece_a_bodega, anada, tipo_de_vino, origen, ano, nombre_de_usuario)    
) COLLATE=utf8_bin;

CREATE TABLE Tipos_Uva (
	id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) UNIQUE
);

CREATE TABLE Vino_Tipo_Uva (
    id_vino INT NOT NULL,
    id_tipo_uva INT NOT NULL,
    FOREIGN KEY (id_vino) REFERENCES Vinos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_tipo_uva) REFERENCES Tipos_Uva(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (id_vino, id_tipo_uva)
);

drop user 'vinos_user'@'localhost';
flush privileges;
create user 'vinos_user'@'localhost' identified BY 'vinos_pass';
grant all privileges on *.* to  'vinos_user'@'localhost' with grant option;
flush privileges;

-- He utilizado la version de MySQL workbench de la maquina virtual facilitada por los profesores de SOS
-- El comando SELECT VERSION(); me ha devuelto'8.0.36-0ubuntu0.22.04.1', si fuese de alguna importancia.

