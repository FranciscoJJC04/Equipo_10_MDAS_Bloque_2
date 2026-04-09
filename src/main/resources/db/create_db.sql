-- phpMyAdmin SQL Dump
-- version 2.7.0-pl2
-- http://www.phpmyadmin.net
-- 
-- Servidor: oraclepr.uco.es
-- Tiempo de generación: 16-11-2025 a las 22:40:48
-- Versión del servidor: 5.1.73
-- Versión de PHP: 5.3.3
-- 
-- Base de datos: `i12rasec`
-- 

-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `alquiler`
-- 

CREATE TABLE `alquiler` (
  `id_alquiler` int(11) NOT NULL AUTO_INCREMENT,
  `matricula` varchar(20) NOT NULL,
  `num_pasajeros` int(11) NOT NULL,
  `importe_total` decimal(10,2) NOT NULL,
  `dni_socio` varchar(12) NOT NULL,
  `fecha_inicio` date DEFAULT NULL,
  `fecha_fin` date DEFAULT NULL,
  PRIMARY KEY (`id_alquiler`),
  KEY `fk_alquiler_socio` (`dni_socio`),
  KEY `fk_alquiler_embarcacion` (`matricula`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8 AUTO_INCREMENT=16 ;

-- 
-- Volcar la base de datos para la tabla `alquiler`
-- 

-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `asignacion`
-- 

CREATE TABLE `asignacion` (
  `fecha_asignacion` date NOT NULL,
  `fecha_fin` date DEFAULT NULL,
  `matricula` varchar(20) NOT NULL,
  `dni_patron` varchar(12) NOT NULL DEFAULT '',
  PRIMARY KEY (`fecha_asignacion`,`matricula`,`dni_patron`),
  KEY `fk_asg_embarcacion` (`matricula`),
  KEY `fk_asg_patron` (`dni_patron`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 
-- Volcar la base de datos para la tabla `asignacion`
-- 

-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `embarcacion`
-- 

CREATE TABLE `embarcacion` (
  `matricula` varchar(20) NOT NULL,
  `tipo` enum('VELERO','YATE','LANCHA','OTRO') NOT NULL,
  `nombre` varchar(80) NOT NULL,
  `num_plazas` int(11) NOT NULL,
  PRIMARY KEY (`matricula`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 
-- Volcar la base de datos para la tabla `embarcacion`
-- 


-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `inscripcion`
-- 

CREATE TABLE `inscripcion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tipo` enum('Individual','Familiar') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32359 DEFAULT CHARSET=utf8 AUTO_INCREMENT=32359 ;

-- 
-- Volcar la base de datos para la tabla `inscripcion`
-- 

-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `patron`
-- 

CREATE TABLE `patron` (
  `dni_patron` varchar(12) NOT NULL,
  `nombre` varchar(60) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `fecha_nacimiento` date NOT NULL,
  PRIMARY KEY (`dni_patron`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 
-- Volcar la base de datos para la tabla `patron`
-- 



-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `reserva`
-- 

CREATE TABLE `reserva` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `dni_socio` varchar(12) NOT NULL,
  `matricula` varchar(20) NOT NULL,
  `importe_reserva` decimal(10,2) DEFAULT NULL,
  `num_pasajeros_reserva` int(11) DEFAULT NULL,
  `descripcion_reserva` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_reserva_socio` (`dni_socio`),
  KEY `fk_reserva_asignacion` (`fecha`,`matricula`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8 AUTO_INCREMENT=26 ;

-- 
-- Volcar la base de datos para la tabla `reserva`
-- 



-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `socio`
-- 

CREATE TABLE `socio` (
  `dni` varchar(12) NOT NULL,
  `nombre` varchar(60) NOT NULL,
  `apellidos` varchar(100) NOT NULL DEFAULT '',
  `fecha_nacimiento` date NOT NULL,
  `direccion` varchar(200) NOT NULL,
  `titulo_patron` tinyint(1) NOT NULL DEFAULT '0',
  `cuota_inscripcion` decimal(8,2) DEFAULT NULL,
  `fecha_inscripcion` date DEFAULT NULL,
  `id_inscripcion` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`dni`),
  KEY `fk_socio_inscripcion` (`id_inscripcion`)
) ENGINE=InnoDB AUTO_INCREMENT=32359 DEFAULT CHARSET=utf8 AUTO_INCREMENT=32359 ;

-- 
-- Volcar la base de datos para la tabla `socio`
-- 



-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `socio_alquiler`
-- 

CREATE TABLE `socio_alquiler` (
  `dni` varchar(12) NOT NULL,
  `id_alquiler` int(11) NOT NULL,
  PRIMARY KEY (`dni`,`id_alquiler`),
  KEY `fk_socio_alquiler_alquiler` (`id_alquiler`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 
-- Volcar la base de datos para la tabla `socio_alquiler`
-- 



-- 
-- Filtros para las tablas descargadas (dump)
-- 

-- 
-- Filtros para la tabla `alquiler`
-- 
ALTER TABLE `alquiler`
  ADD CONSTRAINT `fk_alquiler_embarcacion` FOREIGN KEY (`matricula`) REFERENCES `embarcacion` (`matricula`),
  ADD CONSTRAINT `fk_alquiler_socio` FOREIGN KEY (`dni_socio`) REFERENCES `socio` (`dni`);

-- 
-- Filtros para la tabla `asignacion`
-- 
ALTER TABLE `asignacion`
  ADD CONSTRAINT `fk_asg_embarcacion` FOREIGN KEY (`matricula`) REFERENCES `embarcacion` (`matricula`),
  ADD CONSTRAINT `fk_asg_patron` FOREIGN KEY (`dni_patron`) REFERENCES `patron` (`dni_patron`);

-- 
-- Filtros para la tabla `reserva`
-- 
ALTER TABLE `reserva`
  ADD CONSTRAINT `fk_reserva_socio` FOREIGN KEY (`dni_socio`) REFERENCES `socio` (`dni`);

-- 
-- Filtros para la tabla `socio`
-- 
ALTER TABLE `socio`
  ADD CONSTRAINT `fk_socio_inscripcion` FOREIGN KEY (`id_inscripcion`) REFERENCES `inscripcion` (`id`);

-- 
-- Filtros para la tabla `socio_alquiler`
-- 
ALTER TABLE `socio_alquiler`
  ADD CONSTRAINT `fk_socio_alquiler_socio` FOREIGN KEY (`dni`) REFERENCES `socio` (`dni`),
  ADD CONSTRAINT `fk_socio_alquiler_alquiler` FOREIGN KEY (`id_alquiler`) REFERENCES `alquiler` (`id_alquiler`);
-- -- 
-- -- Estructura de tabla para la tabla `alquiler`
-- -- 
