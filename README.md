# Migrate

## Descripción General
Este programa está específicamente diseñado y dedicado para facilitar el proceso de migraciones de versiones del gestor
documental. Su principal objetivo es garantizar una transición sin problemas de una versión a otra, preservando la
integridad y seguridad de los documentos durante todo el proceso.

## Uso
Para utilizar el programa, necesitarás proporcionar dos argumentos.

- -a1 o —api1, este argumento se refiere a la API a partir de la cual se realizará la migración. (origen)
- -a2 o —api2, este argumento se refiere a la API a la que se migrará. (destino)
- -d o —url, URL de la base de datos sin jdbc:mysql://
- -u o —user, Nombre del usuario en la base de datos.
- -p o —pass, Contraseña de la base de datos.
- -as o —apis, URL base de la api de seguridad.
- -us o —userSecurity, Usuario con el que se va hacer la conexión con la api de seguridad
- -ps o —passSecurity, Contraseña del usuario con el que se va hacer la conexión con la api de seguridad

Aquí tienes un ejemplo del comando que deberías usar:

> java -jar migrate-0.0.1-SNAPSHOT.jar -a1 https://desarrollo.emisuite.es/snc-document-manager-api/ 
> -a2 http://127.0.0.1:12345/emisuite-documentmanager-api/ -d localhost:3306/document_manager -u root -p root 
> -as https://desarrollo.emisuite.es/snc-security-ws/ -us root -ps root

nota: si se levanta la api en local para hacer test para debugear por ejemplo la -a1 no tiene contexto es decir no 
hay que poner /snc-document-manager-api

Al iniciar el programa, se te solicitarán unas ubicaciones especificas. La primera ubicación se usará como punto de
partida para recorrer y migrar todos los archivos al nuevo sistema. La segunda ubicación será la que se establecerá o se
usará para almacenar los archivos y carpetas.

Después cuando se complete al 100% la barra de progreso, Se crear y se moverá todo a a la nueva estructura de carpetas, 
las capetas que no existan se crearan vacías, además Visual SGA y Easy GMAO se buscaran y crearan automáticamente con 
sus UUIDs hardcodeados. después Saldrá una notificación, esta notificación indica que tienes que tener el config.json a 
mano para poder hacer los siguientes pasos.

Se te preguntaran los ids de digital people, si alguno de los ids se repite no hace falta que se introduzca de nuevo.

Cuando se hayan copiado todos los archivos y carpetas, el programa le preguntará si desea eliminarlos de la ubicación 
anterior. Tendrá dos opciones: Y (Sí) o N (No). Si elige otra opción, se le seguirá preguntando hasta que seleccione 
una válida. Si elige Y, se le solicitará una confirmación para prevenir borrados accidentales. Se aconseja seleccionar 
N para evitar la eliminación en caso de que surjan problemas.

Todos los ficheros y carpetas que no se encuentren durante la migración se moverán automáticamente a notFound.