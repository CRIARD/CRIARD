#include <DHT.h>
#include <Servo.h>
#include <SoftwareSerial.h>
#include <SPI.h>
#include <DHT.h>
#include "melodias.h"

//Pines utilizados:
#define PinServo      8   //Pin donde está conectado el servo 
#define pinMicro      A3  //Pin donde está conectado el servo 
#define PinLDR        A1  // Pin donde esta conectado el LDR
#define PinLED        6   // Pin donde esta conectado el LED (PWM)
#define PinLED2        13   // Pin donde esta conectado el LED que se activa con sensor proximidad desde celular
#define PinBuzzer     12  // Pin donde esta conectado el buzzer
#define sensorMojado  9   //variables de humedad
#define DHTPIN        2   // Definimos el pin digital donde se conecta el sensor

//Banderas de sensores entre Android y Arduino
int flagNotificacion = 0;
int flagNotificacionMojado = 0;
int flagNotificacionLuz=0;
int flagNotificacionInicial = 0;
int flagBuzzer;


//Variables del Servo
#define ServoCerrado  90  // posición inicial 
#define ServoQUIETO  90   // posición inicial 
#define ServoAbierto  180 // posición de 0 grados
int tiempoInicialAmaque = 0;
int tiempoAmaque = 0;
int prendoCuna = 0;
int hamacar = 0;   //flag para hamacar izquierda o derecha
int posicionServo = ServoQUIETO; //Va a contener la ubicación del servo
Servo servoMotor;

//Variables del micrófono
#define standby 20000
double valorLlanto = 0;      //variable to store the value coming from the sensor
int tiempoUltimoLlanto = 0;
int tiempoSilencio = 0;
int tiempoInicioProm = 0; // para calcular el ruido ambiente
int tiempoFinProm = 0;
double muestras = 0;
double sumaRuido = 0;
double ruidoPromedio = 0;  
double umbralRuidol= 80;

//Variables LDR
#define ESPERA_LECTURAS 2000 // tiempo en milisegundos entre lecturas de la intensidad de la luz
long cronometro_lecturas=0;
long tiempo_transcurrido;
unsigned int luminosidad;
double coeficiente_porcentaje=255.0/1023.0; //100.0/1023.0; // El valor de la entrada analógica va de 0 a 1023 y se quiere convertir a porcentaje que va de cero a 100

//Variables de mensajes Bluetooth
#define encenderCunaBT  '1'
#define apagarCunaBT    '2'
#define encenderLEDBT   '3'
#define apagarLEDBT     '4'
#define encenderMusicBT '5'
#define apagarMusicBT   '6'

//Variables de estado de cada sensor//
String ESTADOLED = "";
String ESTADOSERVO = "";
String ESTADOMICRO = "";
String ESTADOCOLCHON = "";

String SERVOENCENDIDO  = "Q";
String SERVOAPAGADO    = "W";
String LUZENCENDIDA    = "E";
String LUZAPAGADA      = "R";
String MICROENCENDIDO  = "T";
String MICROAPAGADO    = "Y";
String COLCHONMOJADO   = "U";
String COLCHONSECO     = "I";
String MENSAJE = "#";
int tiempoInicioConex = 0;


//Al utilizar la biblioteca SoftwareSerial los pines RX y TX para la transmicion serie de Bluethoot se pueden cambiar mapear a otros pines.
//Sino se utiliza esta bibioteca esto no se puede realizar y se debera conectar al pin 0 y 1, conexion Serie no pudiendo imprmir por el monitor serie
//Al estar estos ocupados.
SoftwareSerial BTserial(10,11); // RX | TX


char c = ' ';
int flag = 1;
//Humedad y temperatura ambiental
#define DHTTYPE DHT11 // Dependiendo del tipo de sensor
DHT dht(DHTPIN, DHTTYPE);// Inicializamos el sensor DHT11



void setup()
{
    //Se configura la velocidad del puerto serie para poder imprimir en el puerto Serie
    Serial.begin(9600);
    Serial.println("Inicializando configuracion del HC-05...");
  
    //Se configura la velocidad de transferencia de datos entre el Bluethoot  HC05 y el de Android.
    BTserial.begin(9600); 
    Serial.println("Esperando Comandos AT...");

    servoMotor.attach(PinServo); // el servo trabajará desde el pin definido como PinServo
    servoMotor.write(ServoCerrado);   // Desplazamos a la posición 0
    flagBuzzer=0;
    pinMode(PinLDR,INPUT); //Defino el tipo de pin para el LDR (Entrada)
    pinMode(PinLED,OUTPUT); //Defino el tipo de pin para el LED (Salida)
     pinMode(PinLED2,OUTPUT); //Defino el tipo de pin para el LED (Salida)
    pinMode(sensorMojado, INPUT);  //definir pin como entrada
    dht.begin();// Comenzamos el sensor DHT
    analogWrite(PinLED2,255); 
    
    iniciarMelodia(PinBuzzer);
    tiempoInicioConex = millis();
    tiempoInicialAmaque = millis();
    tiempoSilencio = millis();
    tiempoUltimoLlanto = millis();
    tiempoInicioProm = millis();
    tiempoFinProm = 0; 

    
}
 
void loop()
{    
   //sI reciben datos del HC05 
    if (BTserial.available())
    {                
      //se los lee y se los muestra en el monitor serie
      c = BTserial.read();    
      analizarDato(c);   
      Serial.println("Envio de informe");
      if(c == "#"){
        Serial.println("Envio de informe");
        informarEstadoSensor();  
      }          
    }
    escucharLlanto();
    hamacarCuna(); 
    detectarLuz();
    detectarMojado();
}
/**Funcion que utiliza el BT para determinar la accion a realizar**/

//ENVIAR ESTADOS A LA APLICACION
void informarEstadoSensor(){
    //BTserial.write("0");//Dato que envio de entrada para que no me borre el primer caracter del mensaje
    if(ESTADOSERVO == "ON"){
      MENSAJE += SERVOENCENDIDO;
    }else{
      MENSAJE += SERVOAPAGADO;
      }
    if(ESTADOMICRO == "ON"){
      MENSAJE += MICROENCENDIDO;
    }else{
      MENSAJE += MICROAPAGADO;
      }
    if(ESTADOLED == "ON"){
      MENSAJE += LUZENCENDIDA;
    }else{
      MENSAJE += LUZAPAGADA;
      }
    if(ESTADOCOLCHON == "ON"){
      MENSAJE += COLCHONMOJADO;
      ESTADOCOLCHON = "OFF";
    }else{
      MENSAJE += COLCHONSECO;
      }
    //String temp = String(dht.readTemperature());
    //MENSAJE +=  "T" + temp[0] + temp[1];
    enviarEstadoActualAANDROID(MENSAJE); 
    BTserial.write('\n');
    Serial.println(MENSAJE);
    MENSAJE = "#";
    BTserial.flush();
  }


void enviarEstadoActualAANDROID(String msj){
    BTserial.print(msj);     
}

void analizarDato(char c)
{
  switch(c){
      case encenderCunaBT:
      Serial.println("Solicitud recibida: " + c);
        tiempoInicialAmaque = millis();
        ESTADOSERVO = "ON"; 
        break;        
      case apagarCunaBT:
      Serial.println("Solicitud recibida: " + c);
        ESTADOSERVO = "OFF";
        break;
      case encenderLEDBT:
      Serial.println("Solicitud recibida: " + c);
        analogWrite(PinLED2,0); 
        break;
      case apagarLEDBT:
      Serial.println("Solicitud recibida: " + c);
        analogWrite(PinLED2,255); 
        break;
      case encenderMusicBT:
      Serial.println("Solicitud recibida: " + c);
        
        if(flagBuzzer==0){
         sonarMelody4();
          sonarMelody4();
          }
        flagBuzzer=1;
        break;
      case apagarMusicBT:
      Serial.println("Solicitud recibida: " + c);
        apagarMelody();
        break;
      default:
        Serial.print(c);
        break;
    }
}

/**DECLARACION DE FUNCIONES**/  
void detectarLuz(){
  int valor; // Variable para cálculos.
   tiempo_transcurrido=millis()-cronometro_lecturas;    
    if(tiempo_transcurrido>ESPERA_LECTURAS){// espera no bloqueante
        cronometro_lecturas=millis();
        luminosidad=analogRead(PinLDR);
    }
    //*************************Comentado por Vale***********************//
    //le pasamos el valor de luminosidad al ldr
    double swi= luminosidad*coeficiente_porcentaje;
    
    if(swi < 130.0){
      ESTADOLED = "OFF";
      if(flagNotificacionLuz==1){
        informarEstadoSensor(); 
        flagNotificacionLuz=0;
            }
         analogWrite(PinLED,255);
    }else{
      ESTADOLED = "ON";
       if(flagNotificacionLuz==0){
        informarEstadoSensor(); 
        flagNotificacionLuz=1;
            }
            
      analogWrite(PinLED,swi);  
      if(swi>225.0){
        analogWrite(PinLED,0);  
      }
    }
}
  
void hamacarCuna(){
  if(ESTADOSERVO == "ON"){
    if(flagNotificacion == 0 ){
      informarEstadoSensor(); 
      flagNotificacion = 1;
    } 
    tiempoAmaque = millis()-tiempoInicialAmaque;
    if(tiempoAmaque > 20){
    if(hamacar==0){
      posicionServo ++;
      servoMotor.write(posicionServo); 
      if(posicionServo == ServoAbierto){
        hamacar=1;
      }
    }else{
      posicionServo --;
      servoMotor.write(posicionServo);
      if(posicionServo == ServoCerrado){
        hamacar=0;
      }
    }
    tiempoInicialAmaque=millis();
    }
  }else{
    if(flagNotificacion == 1 ){
      informarEstadoSensor(); 
      flagNotificacion = 0;
    } 
      servoMotor.write(ServoQUIETO); 
    }  
}

void escucharLlanto(){
  valorLlanto = (double)analogRead (pinMicro); 
  if(muestras == 2000){
    ruidoPromedio = sumaRuido/muestras;  
    umbralRuidol = ruidoPromedio+ruidoPromedio/2;

    muestras = 0;
    sumaRuido = 0;
  }else{
    muestras = muestras +1 ;
    sumaRuido = sumaRuido + valorLlanto;
  }
  
  if(umbralRuidol!=0 && (valorLlanto>umbralRuidol) ){
   ESTADOSERVO = "ON";
   ESTADOMICRO = "ON";
    tiempoUltimoLlanto = millis();
    tiempoInicialAmaque = millis();
  }else{
    tiempoSilencio = millis();
  }
  //si despues de cierto tiempo no llora apago la mecedora
  if((tiempoSilencio - tiempoUltimoLlanto)> standby ){
    ESTADOSERVO = "OFF";
    ESTADOMICRO = "OFF";
    tiempoSilencio = millis();
    tiempoUltimoLlanto = millis(); 
  }
}

void detectarMojado(){
  int value;
  value = digitalRead(sensorMojado);  //lectura digital de pin
  if (value == LOW) {
       ESTADOCOLCHON = "ON";
       if(flagNotificacionMojado == 0 ){
          informarEstadoSensor(); 
          flagNotificacionMojado = 1;
       } 
  }
    if (value == HIGH) {
       ESTADOCOLCHON = "OFF";
       if(flagNotificacionMojado == 1 ){
          informarEstadoSensor(); 
          flagNotificacionMojado = 0;
       } 
  }
}
