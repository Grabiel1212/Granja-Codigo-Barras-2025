/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  juang
 * Created: 20 jul 2025
 */

 DROP database  ULTIMARQUETSAC;
CREATE DATABASE ULTIMARQUETSAC;
USE ULTIMARQUETSAC;

-- 1. Archivos generados
CREATE TABLE IF NOT EXISTS ArchivosExcel (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    NombreArchivo VARCHAR(255) NOT NULL,
    FechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Códigos generados
CREATE TABLE IF NOT EXISTS CodigosGenerados (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Codigo VARCHAR(100) NOT NULL UNIQUE,
    FechaGenerado DATETIME DEFAULT CURRENT_TIMESTAMP,
    ArchivoID INT NOT NULL,
    FOREIGN KEY (ArchivoID) REFERENCES ArchivosExcel(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- 3. Productos asociados al código generado
CREATE TABLE IF NOT EXISTS Productos (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    CodigoGeneradoID INT NOT NULL UNIQUE,
    NombreProducto VARCHAR(255),
    FOREIGN KEY (CodigoGeneradoID) REFERENCES CodigosGenerados(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

