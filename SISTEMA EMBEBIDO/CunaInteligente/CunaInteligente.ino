//Fuente https://programarfacil.com/tutoriales/fragmentos/servomotor-con-arduino/
#include <Servo.h>
#define ESPERA_LECTURAS 2000 // tiempo en milisegundos entre lecturas de la intensidad de la luz
//Variables del Servo
#define PinServo      9   //Pin donde está conectado el servo 
#define ServoCerrado  0   // posición inicial 
#define ServoAbierto  180  // posición de 0 grados
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
#define PinLED 11 // Pin donde esta conectado el LED (PWM)
long cronometro_lecturas=0;
long tiempo_transcurrido;
unsigned int luminosidad;
float coeficiente_porcentaje=100.0/1023.0; // El valor de la entrada analógica va de 0 a 1023 y se quiere convertir a porcentaje que va de cero a 100

#include <SoftwareSerial.h>
#include <ArduinoJson.h> 
#include <SPI.h>
#include <Ethernet.h>
 // arduino Rx (pin 2) ---- ESP8266 Tx
 // arduino Tx (pin 3) ---- ESP8266 Rx
SoftwareSerial esp8266(3,2); 

int conexionID = 0;
String peticion = "";
void setup()
{
  Serial.begin(115200);  // monitor serial del arduino
  esp8266.begin(115200); // baud rate del ESP8255

  servoMotor.attach(PinServo); // el servo trabajará desde el pin definido como PinServo
  servoMotor.write(ServoCerrado);   // Desplazamos a la posición 0

  pinMode(PinLDR,INPUT); //Defino el tipo de pin para el LDR (Entrada)
  pinMode(PinLED,OUTPUT); //Defino el tipo de pin para el LED (Salida)
  
  tiempoInicialAmaque = millis();
  tiempoSilencio = millis();
  tiempoUltimoLlanto = millis();
  tiempoInicioProm = millis();
  tiempoFinProm = 0; 
  
  sendData("AT+RST\r\n",2000);      // resetear módulo
  sendData("AT+CWSAP=\"ARDUINO\",\"soa-criard-266\",3,2\r\n",8000); 
  sendData("AT+CWMODE=2\r\n",1000); // configurar como cliente
  //sendData("AT+CWJAP=\"Speedy-AC6CA3\",\"matiasmanda\"\r\n",8000); //SSID y contraseña para unirse a red 
  sendData("AT+CIFSR\r\n",1000);    // obtener dirección IP
  sendData("AT+CIPMUX=1\r\n",1000); // configurar para multiples conexiones
  sendData("AT+CIPSERVER=1,80\r\n",1000);         // servidor en el puerto 80
}


void loop()
{

  detectarCliente();

}


/**DECLARACION DE FUNCIONES**/
void encenderLEDGradual(float luz){
  
    analogWrite(PinLED,luz);   
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

/*
Enviar comando al esp8266 y verificar la respuesta del módulo, todo esto dentro del tiempo timeout
*/
void sendData(String comando, const int timeout)
{
 long int time = millis(); // medir el tiempo actual para verificar timeout
 
 esp8266.print(comando); // enviar el comando al ESP8266
 
 while( (time+timeout) > millis()) //mientras no haya timeout
 {
     while(esp8266.available()) //mientras haya datos por leer
     { 
     // Leer los datos disponibles
     char c = esp8266.read(); // leer el siguiente caracter
     Serial.print(c);
     }
 } 
 return;
}

int analizarPeticion(){
  
       esp8266.find("led="); // bucar el texto "led="
       int state = (esp8266.read()-48); // Obtener el estado del pin a mostrar
      
       //digitalWrite(13, state); // Cambiar estado del pin
       while(esp8266.available()){
          char c = esp8266.read();
          Serial.print(c);
      } 
      return state;
  }
String construirRespuesta(int state){
  
        //responder y cerrar la conexión para que el navegador no se quede cargando 
        // página web a enviar
        String webpage = "";
        if (state==1) webpage += "<h1>Led = encendido!</h1>";
        else { webpage += "<h1>Led = apagado!</h1>";}
        
       return webpage;
       
  }
bool enviarRespuesta(String respuesta){
  
      // comando para enviar página web
      String comandoWebpage = "AT+CIPSEND=";
      comandoWebpage+=conexionID;
      comandoWebpage+=",";
      comandoWebpage+=respuesta.length();
      comandoWebpage+="\r\n";
      sendData(comandoWebpage,1000);
      sendData(respuesta,1000);
  }
bool cerrarConexion(){
         // comando para terminar conexión
       String comandoCerrar = "AT+CIPCLOSE=";
       comandoCerrar+=conexionID;
       comandoCerrar+="\r\n";
       sendData(comandoCerrar,3000);
  }
bool detectarCliente(){

  
  if(esp8266.find("+IPD,")) // revisar si el servidor recibio datos
     {
         delay(1500); // esperar que lleguen los datos hacia el buffer
         int conexionID = esp8266.read()-48; // obtener el ID de la conexión para poder responder
         int state = analizarPeticion();
         String respuesta = construirRespuesta(state);
         enviarRespuesta(respuesta);
         cerrarConexion();
     }
  
  }
