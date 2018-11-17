#include <SoftwareSerial.h>
#include <ArduinoJson.h>
//#include "melodias.h"
int flagNotificacion = 0;
int flagNotificacionMojado = 0;
int flagNotificacionLuz=0;
//Fuente https://programarfacil.com/tutoriales/fragmentos/servomotor-con-arduino/
#include <Servo.h>
#define ESPERA_LECTURAS 2000 // tiempo en milisegundos entre lecturas de la intensidad de la luz
//Variables del Servo
#define PinServo      8   //Pin donde está conectado el servo 
#define ServoCerrado  0   // posición inicial 
#define ServoQUIETO  90   // posición inicial 
#define ServoAbierto  180 // posición de 0 grados
int tiempoInicialAmaque = 0;
int tiempoAmaque = 0;
int prendoCuna = 0;
int amacar = 0;   //flag para amacar izquierda o derecha
int posicionServo = ServoQUIETO; //Va a contener la ubicación del servo
Servo servoMotor;

//Variables del micrófono
#define pinMicro      A3   //Pin donde está conectado el servo 
//#define umbralRuido   20  //Valor que lee el microfono en silencio
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
#define PinLDR A1 // Pin donde esta conectado el LDR
#define PinLED 6 // Pin donde esta conectado el LED (PWM)
long cronometro_lecturas=0;
long tiempo_transcurrido;
unsigned int luminosidad;
float coeficiente_porcentaje=100.0/1023.0; // El valor de la entrada analógica va de 0 a 1023 y se quiere convertir a porcentaje que va de cero a 100

#include <SoftwareSerial.h>
#include <SPI.h>
#include <Ethernet.h>
 // arduino Rx (pin 2) ---- ESP8266 Tx
 // arduino Tx (pin 3) ---- ESP8266 Rx

//Variables del led
const int ledPIN = 12;

//variables del buzzer
#define PinBuzzer 4 // Pin donde esta conectado el buzzer
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

String SERVOENCENDIDO  = "E";
String SERVOAPAGADO    = "A";
String LUZENCENDIDA    = "E";
String LUZAPAGADA      = "A";
String MICROENCENDIDO  = "E";
String MICROAPAGADO    = "A";
String COLCHONMOJADO   = "M";
String COLCHONMOSECO   = "S";

int tiempoInicioWIFI = 0;
int tiempoWIFI = 0;
#define DEBUG true
int conexionID = 0;
String peticion = "";
bool estadoConexion = false;
//Al utilizar la biblioteca SoftwareSerial los pines RX y TX para la transmicion serie de Bluethoot se pueden cambiar mapear a otros pines.
//Sino se utiliza esta bibioteca esto no se puede realizar y se debera conectar al pin 0 y 1, conexion Serie no pudiendo imprmir por el monitor serie
//Al estar estos ocupados.
SoftwareSerial BTserial(10,11); // RX | TX

//variables de humedad
const int sensorMojado = 9;

char c = ' ';
int flag = 1;
//HUmedad y temperatura ambiental
#include <DHT12.h>
#include <Wire.h>     //The DHT12 uses I2C comunication.
DHT12 dht12;          //Preset scale CELSIUS and ID 0x5c.
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
  
    pinMode(PinLDR,INPUT); //Defino el tipo de pin para el LDR (Entrada)
    pinMode(PinLED,OUTPUT); //Defino el tipo de pin para el LED (Salida)
    
    tiempoInicioWIFI = millis();
    tiempoInicialAmaque = millis();
    tiempoSilencio = millis();
    tiempoUltimoLlanto = millis();
    tiempoInicioProm = millis();
    tiempoFinProm = 0; 

    pinMode(sensorMojado, INPUT);  //definir pin como entrada
  //Wire para sensor de humedad
  Wire.begin();
}
 
void loop()
{
    
   //sI reciben datos del HC05 
    if (BTserial.available())
    {                
      //se los lee y se los muestra en el monitor serie
      c = BTserial.read();    
      analizarDato(c);   
      informarEstadoSensor();            
    }   
    escucharLlanto();
    amacarCuna(); 
    detectarLuz();
    detectarMojado();
    String ambiente = "0T"  + String(dht12.readTemperature()) + "H"  + String(dht12.readHumidity());
    Serial.println(ambiente);
    //sensoresAmbientales();
}
/**Funcion que utiliza el BT para determinar la accion a realizar**/

//ENVIAR ESTADOS A LA APLICACION
void informarEstadoSensor(){
    BTserial.write("0");//Dato que envio de entrada para que no me borre el primer caracter del mensaje
    if(ESTADOSERVO == "ON"){
      enviarEstadoActualAANDROID(SERVOENCENDIDO); 
    }else{
      enviarEstadoActualAANDROID(SERVOAPAGADO); 
      }
    if(ESTADOMICRO == "ON"){
      enviarEstadoActualAANDROID(MICROENCENDIDO); 
    }else{
      enviarEstadoActualAANDROID(MICROAPAGADO); 
      }
    if(ESTADOLED == "ON"){
      enviarEstadoActualAANDROID(LUZENCENDIDA); 
    }else{
      enviarEstadoActualAANDROID(LUZAPAGADA); 
      }
     if(ESTADOCOLCHON == "ON"){
      enviarEstadoActualAANDROID(COLCHONMOJADO); 
      ESTADOCOLCHON = "OFF";
    }
    
    //String ambiente = "0T" + String(dht12.readTemperature()) + "H" + String(dht12.readHumidity());
    //enviarEstadoActualAANDROID(ambiente); 
    BTserial.write('\n');
    BTserial.flush();
  }


void enviarEstadoActualAANDROID(String msj){
    BTserial.print(msj);     
}

void analizarDato(char c)
{
  switch(c){
      case encenderCunaBT:
        tiempoInicialAmaque = millis();
        ESTADOSERVO = "ON"; 
        //enviarEstadoActualAANDROID(SERVOENCENDIDO);
        break;        
      case apagarCunaBT:
        ESTADOSERVO = "OFF";
        flagNotificacion = 0;
        //enviarEstadoActualAANDROID(SERVOAPAGADO); 
        break;
      case encenderLEDBT:
        analogWrite(PinLED,255); 
        //enviarEstadoActualAANDROID(LUZENCENDIDA); 
        break;
      case apagarLEDBT:
        analogWrite(PinLED,0); 
        //enviarEstadoActualAANDROID(LUZAPAGADA); 
        break;
      case encenderMusicBT:
        //sonarMelody1();
        //enviarEstadoActualAANDROID(MICROENCENDIDO); 
        break;
      case apagarMusicBT:
        //apagarMelody();
        //enviarEstadoActualAANDROID(MICROAPAGADO); 
        break;
    }
}



/**DECLARACION DE FUNCIONES**/  
void detectarLuz(){
   tiempo_transcurrido=millis()-cronometro_lecturas;    
    if(tiempo_transcurrido>ESPERA_LECTURAS){// espera no bloqueante
      
        cronometro_lecturas=millis();
        luminosidad=analogRead(PinLDR);
    }
    double swi = 255-(((luminosidad*coeficiente_porcentaje)*255)/100);
    if(swi > 110){
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
    }

}
  


void amacarCuna(){
  if(ESTADOSERVO == "ON"){
    tiempoAmaque = millis()-tiempoInicialAmaque;
    if(tiempoAmaque > 20){
    if(amacar==0){
      posicionServo ++;
      servoMotor.write(posicionServo); 
      if(posicionServo == ServoAbierto){
        amacar=1;
      }
    }else{
      posicionServo --;
      servoMotor.write(posicionServo);
      if(posicionServo == ServoCerrado){
        amacar=0;
      }
    }
    tiempoInicialAmaque=millis();
    }
  }else{
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
    if(flagNotificacion == 0 ){
      informarEstadoSensor(); 
      flagNotificacion = 1;
    } 
    tiempoUltimoLlanto = millis();
  }else{
    tiempoSilencio = millis();
  }
  //si despues de cierto tiempo no llora apago la mecedora
  if((tiempoSilencio - tiempoUltimoLlanto)> standby ){
    ESTADOSERVO = "OFF";
    informarEstadoSensor(); 
    flagNotificacion = 0;
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
