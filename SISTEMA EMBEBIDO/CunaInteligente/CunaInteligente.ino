#include <SoftwareSerial.h>
#include <ArduinoJson.h>

//Fuente https://programarfacil.com/tutoriales/fragmentos/servomotor-con-arduino/
#include <Servo.h>
#define ESPERA_LECTURAS 2000 // tiempo en milisegundos entre lecturas de la intensidad de la luz
//Variables del Servo
#define PinServo      8   //Pin donde está conectado el servo 
#define ServoCerrado  75   // posición inicial 
#define ServoAbierto  105  // posición de 0 grados
int tiempoInicialAmaque = 0;
int tiempoAmaque = 0;
int prendoCuna = 0;
int amacar = 0;   //flag para amacar izquierda o derecha
int posicionServo = ServoCerrado; //Va a contener la ubicación del servo
Servo servoMotor;

//Variables del micrófono
#define pinMicro      A5   //Pin donde está conectado el servo 
#define umbralRuido   72  //Valor que lee el microfono en silencio
#define standby 20000
int valorLlanto = 0;      //variable to store the value coming from the sensor
int tiempoUltimoLlanto = 0;
int tiempoSilencio = 0;
int tiempoInicioProm = 0; // para calcular el ruido ambiente
int tiempoFinProm = 0;
int muestras = 0;

//Variables LDR
#define PinLDR A0 // Pin donde esta conectado el LDR
#define PinLED 9 // Pin donde esta conectado el LED (PWM)
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
#define PinLED 9 // Pin donde esta conectado el LED (PWM)


//Variables de mensajes Bluetooth
#define encenderCunaBT  '1'
#define apagarCunaBT    '2'
#define encenderLEDBT   '3'
#define apagarLEDBT     '4'




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

char c = ' ';


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
    pinMode(PinLED, OUTPUT);
}
 
void loop()
{
   //sI reciben datos del HC05 
    if (BTserial.available())
    { 
        //se los lee y se los muestra en el monitor serie
        c = BTserial.read();
        //Serial.write("leyendoAA:");
        Serial.write(c);
        analizarDato(c);
    }
    amacarCuna();
    //Si se ingresa datos por teclado en el monitor serie 
    if (Serial.available())
    {
        //se los lee y se los envia al HC05
        c =  Serial.read();
        Serial.write(c);
        BTserial.write(c); 
    }
    
}
/**Funcion que utiliza el BT para determinar la accion a realizar**/
void analizarDato(char c)
{
  //Serial.write("leyendo");
   if(c== encenderCunaBT )
   {
      Serial.println("Cuna encendida...");
      tiempoInicialAmaque = millis();
      prendoCuna = 1;  
   }
   else if(c== apagarCunaBT)
   {
      Serial.println("Cuna apagada...");
      prendoCuna = 0; 
   }else if(c== encenderLEDBT)
   {
    encenderLEDGradual(HIGH);
   }else if(c== apagarLEDBT)
   {
    apagarLED();
   }
}



/**DECLARACION DE FUNCIONES**/
void encenderLEDGradual(float luz){
    digitalWrite(PinLED,HIGH);   
  }
void apagarLED(){
    digitalWrite(PinLED,LOW);   
  }
float detectarLuz(){
  
    tiempo_transcurrido=millis()-cronometro_lecturas;
    
    if(tiempo_transcurrido>ESPERA_LECTURAS){// espera no bloqueante
      
        cronometro_lecturas=millis();
        luminosidad=analogRead(PinLDR);
        Serial.print("La luminosidad es del ");
        Serial.print(luminosidad*coeficiente_porcentaje);//detectamos el porcentaje de luminosidad
        Serial.println("%");
    }

    return luminosidad*coeficiente_porcentaje;
  
  }

void amacarCuna(){
  if(prendoCuna == 1){
  tiempoAmaque = millis()-tiempoInicialAmaque;
    if(tiempoAmaque>20){
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
  }
  
}

void escucharLlanto(){
 valorLlanto = analogRead (pinMicro);
 
    Serial.println(valorLlanto ,DEC);
  if(valorLlanto>umbralRuido)
  {
    Serial.print("Está haciendo ruido");
    Serial.println(valorLlanto ,DEC);
    prendoCuna = 1;
    tiempoUltimoLlanto = millis();
  }else{
    tiempoSilencio = millis();
  }
  //si despues de cierto tiempo no llora apago la mecedora
  if((tiempoSilencio - tiempoUltimoLlanto)> standby ){
        prendoCuna = 0;
        tiempoSilencio = millis();
        tiempoUltimoLlanto = millis(); 
  }
}
