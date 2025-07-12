![alt text](https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Farmatodo_logo.svg/1280px-Farmatodo_logo.svg.png)
# Revisión de Código

### Design docs asociados -> [Design doc feature](#https://www.farmatodo.com.co)

### Link diagramas de arquitectura -> [Diagramas de arquitectura](#https://app.diagrams.net/#G1lxoDaykEAFELRComHjcLRgdlMb-KHo0H#%7B%22pageId%22%3A%22BD5eS0AnyStvkowXaXu8%22%7D)

# Criterios de revisión
<!-- Sección requerida -->
Certifico que mi solicitud satisface cada uno de los siguientes requerimientos

- [ ] El código sigue las guías de estilo y convenciones del equipo.
- [ ] Los nombres de variables, funciones y clases son representativos y autodescriptivos.
- [ ] El código se ha organizado en módulos cohesivos y reutilizables.
- [ ] Se han evitado repeticiones innecesarias de código.
- [ ] Se han tenido en cuenta las prácticas seguras de codificación y no hay vulnerabilidades conocidas.
- [ ] El código maneja adecuadamente situaciones de error y excepciones.
- [ ] No se producen pérdidas de datos o problemas de estabilidad.
- [ ] Se han incluido pruebas unitarias adecuadas para cubrir la nueva funcionalidad o cambios realizados.
- [ ] Todas las pruebas unitarias pasan con éxito.
- [ ] Las pruebas están bien diseñadas y son legibles.
- [ ] El código está suficientemente documentado, tanto a nivel de código como a nivel de funciones y clases.
- [ ] Se han identificado oportunidades para mejorar el rendimiento, la legibilidad o la simplicidad del código.
- [ ] El código es compatible con las versiones adecuadas de las dependencias y librerías utilizadas.
- [ ] Los valores de entrada están siendo validados.
- [ ] El código está bien diseñado.
- [ ] Cualquier cambio en la interfaz de usuario es sensato y cumple con lo diseñado por el equipo de Ux/Ui
- [ ] Cualquier programación paralela se realiza de forma segura (Thread Safe).
- [ ] El código no es más complejo de lo necesario.
- [ ] El desarrollador no está implementando cosas que podría necesitar en el futuro pero que no sabe que necesita ahora.
- [ ] Los comentarios son claros y útiles, y en su mayoría explican por qué en lugar de qué.

# Evidencias de Seguridad

<details>
<summary>A continuación, se presentan las evidencias de la validación de seguridad manuales y automatizados: </summary>
</details>

# Self - QA

<details>
<summary>A continuación, se presentan las evidencias funcionales del desarrollo realizadas por el mismo desarrollador: </summary>
</details>

# Cross - QA

<details>
<summary>A continuación, se presentan las evidencias funcionales del desarrollo realizadas por un desarrollador diferente al que realizó el feature: </summary>
</details>


# Criterios de seguridad

Certifico que mi solicitud satisface cada uno de los siguientes requerimientos de seguridad y no viola las buenas prácticas aqui definidas. Y de asi serlo, lo he declarado previamente con el equipo de desarrollo y los lideres  de toda el area mediante un mensaje escrito en slack o correo: 

## Validación de entradas

- [ ] Realizar todas las validaciones de datos en un sistema confiable. Por ejemplo: el servidor
- [ ] Identificar todas las fuentes de datos y clasificarlos como confiables o no. Validar todos los datos provenientes de fuentes no confiables. Por ejemplo: bases de datos, archivo, etc
- [ ] Debería existir una rutina de validación de datos de entrada centralizada para la aplicación
- [ ] Para todas las entradas de datos, especificar el juego de caracteres apropiado, tal como UTF-8
- [ ] Codificar los datos a un juego de caracteres común antes de su validación (canonicalización)
- [ ] Todas las fallas en la validación deber terminar en el rechazo del dato de entrada
- [ ] Determinar si el sistema soportará juegps de caracteres UTF-8 extendidos y de ser así, validarlos luego de terminada la decodificación del UTF-8
- [ ] Validar todos los datos brindados por el cliente antes de procesarlos, incluyendo todos los parámetros, URLs y contenidos de encabezados HTTP (por ejemplo nombres de Cookies y valores). Asegurarse de incluir los pedidos automáticos generados por JavaScript, Flash u otro código embebido
- [ ] Verificar que los valores de los encabezados, tanto en solicitudes como en respuestas contengan sólo caracteres ASCII
- [ ] Validar los datos provenientes de redirecciones. Un atacante puede enviar contenido malicioso directamente en el destino de la redirección, evitando la lógica de la aplicación y cualquier otra validación realizada antes de la redirección
- [ ] Validar que los tipos de datos sean los esperados
- [ ] Validar rangos de datos
- [ ] Validar largos de datos
- [ ] Validar toda entrada con una “lista permitida” que contenga los caracteres aceptados, siempre que sea posible
- [ ] Si es necesario permitir el ingreso de carácteres peligrosos, implementar controles adicionales tales como la codificación de la salida, utilizar una API de seguridad y el registrar del uso de estos datos a través de la aplicación. Algunos ejemplos de caracteres peligrosos: < > " ' % ( ) & + \ \' \"
- [ ] Si su rutina estándar de validación no contempla el ingreso de los ejemplos de datos anteriormente indicados, esta rutina debería de ser revisada puntualmente
- [ ] Compruebe si hay bytes nulos (%00)
- [ ] Compruebe si hay caracteres de nueva línea (%0d, %0a, \r, \n)
- [ ] Compruebe si hay caracteres de alteraciones de ruta “punto, punto, barra” (../ o ..\). En los casos en que se soportan sets de caracteres UTF-8 extendidos, implemente representaciones alternativas tales como: %c0%ae%c0%ae/ (utilice la canonicalización como forma de mitigar la doble codificación u otras formas de ofuscar ataques)

## Codificación de salidas
- [ ] Realice toda codificación en una zona de confianza (Por ejemplo, en el servidor)
- [ ] Utilice una rutina probada y estándar para cada tipo de codificación de salida
- [ ] Codifique las salidas tomando en cuenta el contexto de todos los datos devueltos al cliente que se encuentre fuera de la frontera de confianza de la aplicación. La codificación de entidades HTML es un ejemplo, aunque no sea suficiente en todos los casos
- [ ] Codifique todos los caracteres salvo que sean reconocidos como seguros por el intérprete al que están destinados
- [ ] Sanitice según el contexto todas las salidas de datos no confiables hacia consultas SQL, XML y LDAP
- [ ] Sanitice todas las salidas de datos no confiables hacia comandos del sistema operativo

## Autenticación y gestión de contraseñas
- [ ] Requerir autenticación para todos los recursos y páginas excepto aquellas específicamente clasificadas como públicas
- [ ] Todos los controles de autenticación deben ser efectuados dentro de la frontera de confianza, por ejemplo: el servidor
- [ ] Establecer y utilizar servicios de autenticación estándares y probados cuando sea posible
- [ ] Utilizar una implementación centralizada para todos los controles de autenticación, incluyendo bibliotecas que invoquen a servicios externos de autenticación
- [ ] Segregar la lógica de autenticación, de la del recurso solicitado y utilizar redirecciones desde y hacia el control centralizado de autenticación
- [ ] Todos los controles de autenticación deben fallar de una forma segura
- [ ] Todas las funciones administrativas y de gestión de cuentas deben ser al menos tan seguras como el mecanismo primario de autenticación
- [ ] Si la aplicación gestiona un almacén de credenciales, se debe asegurar que únicamente se almacena el hash con sal de las contraseñas y que el archivo/tabla que guarda las contraseñas y claves solo puede ser escrito por la aplicación (si es posible, no utilizar el algoritmo de hash MD5)
- [ ] El hash de las contraseñas debe ser implementado en dentro de la frontera de confianza. (por ejemplo: el servidor)
- [ ] Validar los datos de autenticación únicamente luego haber completado todos los datos de entrada, especialmente en implementaciones de autenticación secuencial
- [ ] Las respuestas a los fallos en la autenticación no deben indicar cuál parte de la autenticación fue incorrecta. A modo de ejemplo, en lugar de "usuario invalido" o "contraseña invalida", utilizar "usuario y/o contraseña inválidos" en ambos casos. Las repuestas a los errores deben ser idénticas tanto a nivel de lo desplegado como a nivel del código fuente.
- [ ] Utilizar autenticación para conexiones a sistemas externos que involucren información o funciones sensibles.
- [ ] Las credenciales de autenticación para acceder a servicios externos a la aplicación deben ser cifrados y almacenados en ubicaciones protegidas dentro de la frontera de confianza (por ejemplo: el servidor). El código fuente NO es una ubicación segura.
- [ ] Utilizar únicamente pedidos del tipo HTTP POST para la transmisión de credenciales de autenticación.
- [ ] Utilizar únicamente conexiones cifradas o cifrar los datos para el envío de contraseñas que no sean temporales, por ejemplo: correo cifrados. Contraseñas temporales (como aquellas asociadas con reseteos por correo electrónico), pueden ser una excepción.
- [ ] Aplicar por medio de una política o regulación los requerimientos de complejidad de la contraseña. Las credenciales de autenticación deben ser suficientes como para resistir aquellos ataques típicos de las amenazas en el entorno del sistema, por ejemplo: obligar el uso de combinaciones de caracteres numéricos/alfanuméricos y/o caracteres especiales.
- [ ] Aplicar por medio de una política o regulación los requerimientos de longitud de la contraseña. Comúnmente se utilizan ocho caracteres, pero dieciséis es mejor, adicionalmente considerar el uso de frases de varias palabras.
- [ ] No se debe desplegar en la pantalla la contraseña ingresada. A modo de ejemplo, en formularios web, utilizar el tipo de entrada "password" (input type="password").
- [ ] Deshabilitar las cuentas luego de un número establecido de intentos inválidos de inicio de sesión(por ejemplo, cinco intentos). La cuenta debe ser dehabilitada por un periodo de tiempo suficiente como para desalentar una inferencia de credenciales por fuerza bruta, pero no tanto como para provocar un ataque de denegación de servicio.
- [ ] El cambio y reseteo de contraseñas requieren los mismos niveles de control asociados a la creación y autenticación de cuentas.
- [ ] Las preguntas para el reseteo de contraseñas deben contemplar un amplio rango de respuestas aleatorias, por ejemplo: "libro favorito" es una mala pregunta dado que "la biblia" es una respuesta muy común.
- [ ] Si se utiliza la recuperación de contraseña a través del correo electrónico, se debe enviar únicamente un link o contraseña temporal a una casillas previamente registrada en el sistema.
- [ ] Las contraseñas y links temporales deben tener un corto periodo de validez.
- [ ] Obligar el cambio de contraseñas temporales luego de su utilización.
- [ ] Notificar a los usuarios cada vez que se produce un reseteo de contraseña.
- [ ] Prevenir la reutilización de contraseñas.
- [ ] Las contraseñas deben tener al menos un día de antigüedad antes de poder ser cambiadas, de forma de evitar ataques de reutilización de contraseñas.
- [ ] Hacer cumplir por medio de una política o regulación los requerimientos de cambio de contraseña. Los sistemas críticos pueden requerir cambios más frecuentes que otros sistemas. El tiempo entre cada reseteo debe ser controlado administrativamente.
- [ ] Deshabilitar la funcionalidad de "auto completar" campos de contraseñas.
- [ ] El último acceso (fallido o exitoso) debe ser reportado al usuario en su siguiente acceso exitoso.
- [ ] Implementar un monitoreo para identificar ataques a múltiples cuentas utilizando la misma contraseña. Este patrón de ataque es utilizado parar superar bloqueos estándar cuando los nombres de usuario pueden ser recopilados o adivinados.
- [ ] Cambiar todos los usuarios y contraseñas por defecto o deshabilitar las cuentas asociadas
- [ ] Re autenticar usuarios antes de la realización de operaciones críticas.
- [ ] Utilizar autenticación multi-factor para las cuentas más sensibles o de mayor valor.
- [ ] Si se utiliza un código de una tercera parte para la autenticación, inspeccionarlo minuciosamente para asegurar que no se encuentre afectado por cualquier código malicioso.

## Administración de sesiones
- [ ] Utilizar las funciones para la gestión de sesiones propias del framework de desarrollo o del servidor de aplicaciones. Sólo se deben reconocer estos identificadores como válidos
- [ ] La creación de identificadores de sesión solo debe ser realizada dentro de una frontera de confianza (el servidor, por ejemplo)
- [ ] Los controles de gestión de sesiones deben utilizar algoritmos que generen identificadores suficientemente aleatorios
- [ ] Definir el dominio y ruta para las cookies que contienen identificadores de sesión autenticados con un valor suficientemente estricto para el sitio
- [ ] La función de fin de sesión (logout) debe terminar completamente con la sesión o conexión asociada
- [ ] La función de fin de sesión (logout) debe estar disponibleen todas las páginas protegidas por autenticación
- [ ] Establecer un tiempo de inactividad de la sesión lo más corto posible, balanceando los riesgos con los requerimientos del negocio. En la mayoría de los casos, nunca debería ser superior a algunas horas
- [ ] Deshabilitar logeos persistentes y efectuar finalizaciones periódicas de sesiones, incluso cuando la sesión se encuentra activa.Esto aplica especialmente en aplicaciones ricas en conexiones de red o que se conectan a sistemas críticos. El período de tiempo para finalizar la sesión debe de ser compatible con las necesidades del negocio. Además, el usuario debe recibir las notificaciones suficientes para poder mitigar impactos negativos
- [ ] Si una sesión fue establecida antes del inicio de sesión, cerrar dicha sesión y establecer una nueva luego de un login exitoso.
- [ ] Generar un nuevo identificador de sesión luego de cada re autenticación.
- [ ] No permitir logeos concurrentes con el mismo identificador de usuario.
- [ ] No exponer identificadores de sesión en URLs, mensajes de error ni logs. Los identificadores de sesión solo deben ser ubicados en la cookie HTTP. A modo de ejemplo, no transmitir el identificador de sesión como un parámetro GET
- [ ] Proteger de accesos no autorizados los datos de las sesiones, por parte de otros usuarios del servidor, implementando los controles de acceso acordes
- [ ] Generar un nuevo identificador de sesión y desactivar el anterior de forma periódica.Esto puede mitigar algunos escenarios de robo de sesiones donde el identificador es comprometido
- [ ] Generar un nuevo identificador de sesión si la conexión cambia de HTTP a HTTPS, como puede suceder durante la autenticación. Dentro de la aplicación es recomendable utilizar siempre HTTPS en lugar de cambiar entre HTTP y HTTPS
- [ ] Uso de complementos de gestión de sesión para operaciones sensibles del lado del servidor para operaciones sensibles (como la gestión de cuentas de usuarios). Algunos ejemplos son el uso de tokens o parámetros generados aleatoramente para la sesión. Este método puede ser utilizado para prevenir ataques de (Falsificación de petición en sitios cruzados (Cross Site Request Forgery Attacks, o CSRF)
- [ ] Uso de complementos de gestión de sesión para operaciones sensibles o críticas utilizando tokens o parámetros por cada pedido (per request) en lugar de por sesión
- [ ] Configurar el atributo "seguro" (secure) para las cookies transmitidas sobre una conexión TLS
- [ ] Configurar las cookies con el atributo HttpOnly, salvo que se requiera específicamente acceso desde los scripts del lado del cliente, para leer o configurar una cookie

## Control de Acceso
- [ ] Para el flujo decisiones de autorización, utilizar únicamente objetos confiables del sistema. Por ejemplo, objetos de sesión del lado del servidor
- [ ] Utilizar un único componente para el chequeo de autorizaciones para todo el sitio. Esto incluye bibliotecas que invoquen a servicios de autorización externos
- [ ] Los controles de acceso deben fallar de forma segura
- [ ] Denegar todos los accesos en caso que la aplicación no pueda acceder a la información de configuración de seguridad
- [ ] Requerir controles de autorización en cada solicitud, incluyendo aquellos creados por scripts en el servidor, "includes" y pedidos AJAX o Flash desde el lado del cliente
- [ ] Separar lógica privilegiada del restp del código de la aplicación
- [ ] Restringir acceso a archivos u otros recursos, incluyendo aquellos fuera del control directo de la aplicación, únicamente a usuarios autorizados
- [ ] Restringir el acceso a las URL protegidas sólo a los usuarios autorizados
- [ ] Restringir el acceso a las funciones protegidas únicamente a los usuarios autorizados
- [ ] Restringir las referencias directas a objetos únicamente a los usuarios autorizados
- [ ] Restringir el acceso a los servicios sólo a los usuarios autorizados
- [ ] Restringir el acceso a información de la aplicación, solo a usuarios autorizados
- [ ] Restringir el acceso a los atributos de usuario y datos y a la información sobre políticas utilizados por los controles de acceso
- [ ] Restringir el acceso a la información de configuración relevante para la seguridad sólo a usuarios autorizados
- [ ] Las reglas de control de acceso implementadas del lado del servidor y en la capa de presentación, deben coincidir
- [ ] Si los datos de estado deben almacenarse en el cliente, deben ser cifrados y comprobada su integridad en el servidor para detectar manipulaciones
- [ ] Hacer que flujos de aplicación que cumplan con las reglas del negocio
- [ ] Limitar el número de transacciones que un usuario común o un dispositivo puede ejecutar en un período de tiempo dado. La tasa de transacciones/tiempo debe ser mayor a la necesidad del negocio, pero suficientemente bajo para detectar ataques automatizados
- [ ] Utilizar el header "referer" sólo como un chequeo complementario. Nunca debe ser utilizado como chequeo de autorización, ya que puede ser suplantado
- [ ] Si se permiten sesiones autenticadas largas, revalide periódicamente la autorización de un usuario para asegurarse de que sus privilegios no han cambiado y, si es así, cerrar la sesión del usuario y obligarle a volver a autenticarse
- [ ] Implemente la auditoría de cuentas y aplique la desactivación de cuentas no utilizadas (por ejemplo, después de no más de 30 días de la expiración de la contraseña de una cuenta)
- [ ] La aplicación debe permitir deshabilitar y terminar cuentas una vez que se termina la autorización. Por ejemplo ante un cambio de rol, estatus de empleo, etc
- [ ] Cuentas de servicio o cuentas de soporte a la conectividad deben poseer los mínimos privilegios
- [ ] Crear una política de control de acceso para documentar las reglas de negocio de la aplicación, los tipos de datos, criterios para autorización de acceso y los controles asociados para otorgarlos y controlarlos. Esto incluye la identificación de accesos requeridos tanto para los datos como para los sistemas

## Prácticas Critpográficas
- [ ] Todas las funciones de criptografía de la aplicación deben ser implementadas en sistemas de confiables (el servidor, por ejemplo)
- [ ] Proteger secretos maestros de accesos no autorizados
- [ ] Los módulos criptográficos deben fallar en forma segura
- [ ] Todos los números aleatorios, nombres aleatorios de archivos, GUIDs, y cadenas de caracteres aleatorios, deben generarse utilizando el módulos aprobado para tal fin, tendiendo a que los valores generados no sean predecibles
- [ ] Los módulos criptrográficos utilizados por la aplicación deben cumplir con FIPS 140-2 o con su estándar equivalente. Ver NIST Cryptographic Module Validation Program
- [ ] Establecer y utilizar una política y procesos de cómo gestionar las claves criptográficas

## Manejo de errores y Logs
- [ ] No divulgar información sensible en respuestas de error, incluyendo detalles del sistema, identificadores de sesión o información de la cuenta
- [ ] Utilizar manejadores de errores que no muestren información de depuración o de memoria
- [ ] Implementar mensajes de error genéricos y utilizar páginas de error personalizadas
- [ ] La aplicación debe manejar los errores de la aplicación y no basarse en la configuración del servidor
- [ ] Liberar correctamente la memoria asignada cuando se producen condiciones de error
- [ ] La lógica de tratamiento de errores asociada a los controles de seguridad debe denegar el acceso por defecto
- [ ] Todos los controles de registro deben estar implementados en sistemas confiables (por ejemplo, el servidor)
- [ ] El registro de los controles de acceso debe incluir tanto los casos de éxito como de falla
- [ ] Asegurar que los [*datos de registros (logs) contengan información importante
- [ ] Asegurar que los registros de bitácora (logs) de entrada que incluyen información no confiable, no serán interpretado o ejecutado como código en interfaces de visualización o aplicativos
- [ ] Restringir el acceso a los registros de bitácora (logs), sólo a personal autorizado
- [ ] Utilizar una rutina centralizada para todas las operaciones de registro (logging)
- [ ] No guardar información sensible en registros de bitácora (logs), incluyendo detalles innecesarios del sistema
- [ ] Asegurar que existen mecanismos para conducir un análisis de los registros de bitácora (logs)
- [ ] Registrar todas las fallas de validación
- [ ] Registrar todos los intentos de autenticación, en particular los fallidos
- [ ] Registrartodas las fallas en los controles de acceso
- [ ] Registrar todos los eventos de intento de evasión de controles, incluyendo cambios en el estado de la información no esperados
- [ ] Registrar todos los intentos de conexión con tokens inválidos o vencidos
- [ ] Registrar todas las excepciones del sistema
- [ ] Registrar todas las funciones administrativas, incluyendo cambios en la configuración de seguridad
- [ ] Registrar todas las fallas de conexión de TLS
- [ ] Registrar las fallas de los módulos criptográficos
- [ ] Utilizar una función de hash para validar la integridad de los registros (logs)

## Protección de datos
- [ ] Implementar el principio de mínimo privilegio, restringir el acceso de los usuarios solamente a la funcionalidad, datos y sistemas de información que sean necesarios para realizar sus tareas.
- [ ] Proteger todas las copias temporales o en caché de datos sensibles almacenados en el servidor frente a accesos no autorizados.
- [ ] Eliminar los archivos temporales de trabajo cuando ya no sean necesarios.
- [ ] Cifrar toda información altamente sensible almacenada, como datos para la verificación de la autenticación, incluso en el servidor.
- [ ] Utilizar algoritmos de cifrado recomendados (ver "Prácticas Criptográficas").
- [ ] Proteger el código fuente del servidor de forma que no pueda ser descargado por un usuario.
- [ ] No almacenar contraseñas, cadenas de conexión u otra información sensible en texto claro o de cualquier otra forma no criptográfica en el lado del cliente.
- [ ] Remover los comentarios en el código de producción accesible al usuario que puedan revelar detalles sobre los servidores o información sensible.
- [ ] Eliminar cualquier aplicación innecesaria o documentación de sistemas que puedan revelar información útil para los atacantes.
- [ ] No incluir información sensible en los parámetros de la consulta HTTP GET.
- [ ] Deshabilitar la función de autocompletar en formularios que contengan información sensible, incluyendo autenticación.
- [ ] Deshabilitar el caché en el cliente para las páginas que contengan información sensible, usando los encabezados HTTP "Cache-Control: no-store" y "Pragma: no-cache".
- [ ] Implementar mecanismos para eliminar datos sensibles cuando ya no son requeridos, como información personal o ciertos datos financieros.
- [ ] Implementar controles de acceso adecuados para datos sensibles almacenados en el servidor.

## Seguridad en las comunicaciones
- [ ] Implementar cifrado para todas las transmisiones de información sensible, como TLS para proteger la conexión.
- [ ] Los certificados TLS deben ser válidos, poseer el nombre de dominio correcto, no estar expirados e incluir certificados intermedios si son requeridos.
- [ ] Las conexiones TLS fallidas no deben transformarse en una conexión insegura.
- [ ] Utilizar TLS para todo el contenido que requiera acceso autenticado o información sensible.
- [ ] Usar TLS para las conexiones a sistemas externos que involucren funciones o información sensible.
- [ ] Utilizar una implementación estándar única de TLS correctamente configurada.
- [ ] Especificar caracteres de codificación para todas las conexiones.
- [ ] Filtrar los parámetros que contengan información sensible en el encabezado HTTP referer cuando existan vínculos a sitios externos.

## Configuración de los sistemas
- [ ] Asegurarse de que los servidores, frameworks y componentes del sistema están usando la última versión aprobada.
- [ ] Mantener los servidores, frameworks y componentes del sistema actualizados con todos los parches emitidos.
- [ ] Deshabilitar el listado de directorio.
- [ ] Restringir el servidor web, procesos y cuentas de servicios al mínimo privilegio posible.
- [ ] Manejar las excepciones de manera segura, asegurando que falle de forma segura.
- [ ] Eliminar todas las funcionalidades y archivos que no sean necesarios antes del despliegue.
- [ ] Evitar la divulgación de la estructura de directorios a través del archivo robots.txt, deshabilitando el directorio raíz.
- [ ] Definir qué métodos HTTP soportará la aplicación y manejarlos adecuadamente.
- [ ] Desactivar los métodos HTTP innecesarios como WebDAV.
- [ ] Asegurarse de que HTTP 1.0 y 1.1 están configurados de forma similar si ambos son aceptados por el servidor.
- [ ] Eliminar información innecesaria de los encabezados HTTP de respuesta (SO, versiones del servidor).
- [ ] Almacenar la configuración de seguridad en un formato legible para permitir auditoría.
- [ ] Implementar un Sistema de Gestión de Activos para registrar los componentes del sistema.
- [ ] Aislar los ambientes de desarrollo de la red de producción y permitir acceso solo a grupos autorizados.
- [ ] Implementar un Sistema de Control de Cambios del Software para gestionar y registrar los cambios en los ambientes de desarrollo y producción.

## Seguridad de Base de Datos
- [ ] Utilizar consultas parametrizadas con tipos de datos fuertemente tipados.
- [ ] Validar entradas y codificar salidas, manejando correctamente los metacaracteres.
- [ ] Asegurar que todas las variables tengan tipos de datos asociados.
- [ ] La aplicación debe usar el mínimo nivel de privilegios para acceder a la base de datos.
- [ ] Usar credenciales seguras para acceder a la base de datos.
- [ ] No incluir cadenas de conexión a la base de datos en el código de la aplicación; deben estar en un archivo de configuración separado y cifrado.
- [ ] Usar procedimientos almacenados para abstraer el acceso a los datos y eliminar permisos directos de las tablas.
- [ ] Cerrar la conexión a la base de datos lo más pronto posible.
- [ ] Cambiar todas las contraseñas administrativas por defecto y usar contraseñas fuertes o autenticación multifactor.
- [ ] Deshabilitar funcionalidades innecesarias en la base de datos.
- [ ] Deshabilitar cuentas por defecto que no sean necesarias.
- [ ] Conectar a la base de datos con credenciales diferentes para cada nivel de confianza (usuarios, solo lectura, etc.).

## Manejo de Archivos
- [ ] No utilizar información provista por el usuario en operaciones de inclusión dinámica (include).
- [ ] Requerir autenticación antes de permitir la transferencia de archivos al servidor.
- [ ] Limitar los tipos de archivos que pueden ser cargados al servidor a aquellos necesarios.
- [ ] Validar los tipos de archivo transferidos verificando la estructura de sus encabezados.
- [ ] No almacenar archivos cargados en el contexto de la aplicación web; usar un repositorio separado o base de datos.
- [ ] Evitar o restringir la transferencia de archivos que puedan ser interpretados por el servidor web (asp, php, jsp, etc.).
- [ ] Eliminar permisos de ejecución en carpetas donde se cargan archivos.
- [ ] Implementar transferencia de archivos segura en UNIX usando discos lógicos o chroot.
- [ ] Utilizar una lista de nombres y extensiones permitidas para referencias a archivos existentes en el servidor.
- [ ] No usar información provista por el usuario para generar redirecciones dinámicas.
- [ ] No incluir nombres de directorios o rutas de archivos en parámetros; usar índices internos.
- [ ] Nunca enviar la ruta absoluta de un archivo al cliente.
- [ ] Asegurar que los archivos y recursos de la aplicación sean de solo lectura.
- [ ] Analizar los archivos transferidos por los clientes en busca de virus y malware.

## Manejo de Memoria
- [ ] Utilizar controles en las entradas y salidas de datos no confiables.
- [ ] Verificar que el tamaño de los buffers sea el requerido.
- [ ] Truncar la longitud de los strings de entrada antes de pasarlos a funciones de copia o concatenación.
- [ ] Liberar recursos manualmente y no depender del garbage collector (conexiones, archivos, etc.).
- [ ] Utilizar stacks no ejecutables cuando sea posible (NX bit).
- [ ] Evitar el uso de primitivas con vulnerabilidades conocidas (printf, strcat, strcpy, etc.).
- [ ] Liberar adecuadamente la memoria en todos los puntos de finalización.

## Prácticas Generales para la Codificación
- [ ] Utilizar código gestionado probado y aprobado para tareas comunes en lugar de crear nuevo código.
- [ ] Utilizar APIs build-in para acceso a funciones específicas del sistema operativo.
- [ ] Utilizar hashes para verificar la integridad del código interpretado, bibliotecas, ejecutables y archivos de configuración.
- [ ] Usar locks o mecanismos de sincronización para evitar condiciones de carrera.
- [ ] Proteger las variables y recursos compartidos de accesos concurrentes.
- [ ] Inicializar explícitamente todas las variables y almacenes de datos.
- [ ] Las aplicaciones que requieran privilegios elevados deben solicitarlos lo más tarde posible y liberarlos lo antes posible.
- [ ] Evitar errores de cálculo comprendiendo cómo el lenguaje maneja operaciones matemáticas y representaciones numéricas.
- [ ] No utilizar datos proporcionados por el usuario para ejecutar funciones dinámicamente.
- [ ] Revisar todas las aplicaciones secundarias, código de terceros y bibliotecas para validar su funcionamiento seguro.
- [ ] Implementar mecanismos seguros para las actualizaciones automáticas utilizando firmas criptográficas.


Certifico que mi solicitud aplica las tecnicas y tacticas de seguridad y de  ingeniería de software para mitigar los ataques comunes: 

- [ ] Ataques de inyección, incluyendo SQL, LDAP, XPath u otros fallos de flujo de tipo comando, parámetro, objeto, defecto o de inyección.
- [ ] Ataques a datos y estructuras de datos, incluyendo intentos de manipulación de buffers, punteros, datos de entrada o datos compartidos.
- [ ] Ataques al uso de criptografía, incluyendo intentos de explotar implementaciones criptográficas débiles, inseguras o inapropiadas, algoritmos, suites de cifrado o modos de operación
- [ ] Ataques a la lógica del negocio, incluyendo los intentos de abusar o eludir las características y funcionalidades de la aplicación a través de la manipulación de las APIs, los protocolos y canales de comunicación, la funcionalidad del lado del cliente, u otras funciones y recursos del sistema/aplicación. Esto incluye los scripts entre sitios (XSS) y la falsificación de petición entre sitios (CSRF).
- [ ] Ataques a los mecanismos de control de acceso, incluidos los intentos de eludir o abusar de los mecanismos de identificación, autenticación o autorización, o los intentos de aprovechar las debilidades en la implementación de dichos mecanismos.



# Tipo de Solicitud de Cambios
<!-- Sección requerida -->
<!-- Limite sus PRs a un solo tipo, envíe multiples PRs de ser necesario -->
Esta solicitud de cambios contiene:

- [ ] Bugfix
- [ ] Feature
- [ ] Actualización de estilo (formatting, renaming)
- [ ] Refactor (cambios no funcionales)
- [ ] Mejoras de Rendimiento
- [ ] Cambios a nivel de documentación
- [ ] Ajustes de seguridad
- [ ] Otro (por favor describa) ->

# Información Adicional
<!-- Cualquier información que sea importante para este PR. -->
<!-- Adjunta las evidencias que sean necesarias -->

-
-

#  Responsabilidad

<!-- Sección requerida. No borrar. -->

Como **Autor** de este Pull Request **declaro** que:

> - He leído y entiendo las buenas prácticas para la elaboración de una Solicitud de Cambios.

Cómo **Revisor**  de este Pull Request, **al aprobar** este Pull Request **declaro** que:

> - Realicé una revisión de código adecuado, completa, honesta y profesional.
> - **Comparto responsabilidad** con el autor por cualquier código malicioso o malintencionado que pudiera contener.
