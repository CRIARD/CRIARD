//Fuente https://programarfacil.com/tutoriales/fragmentos/servomotor-con-arduino/
#include <Servo.h>
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

//Para la red y comunicación
#include <SoftwareSerial.h>
#define DEBUG true
String sendCommand(String command, const int timeout, boolean debug);
SoftwareSerial esp8266(5,4); // Convierte el pin5 en TX ; convierte el pin4 en RX

void setup() {
  
    /*
    * Configuracion para el Wi-Fi
    * Utilizamos para usar la comunicacion serial
    */
    Serial.begin(115200);     //Iniciando puerto serial
    esp8266.begin(115200);    //Iniciando conexión con módulo WIFI

    //Inicializando red WIFI
    //sendCommand("AT+RST\r\n",2000,DEBUG); // reset module
    //sendCommand("AT+CWMODE=3\r\n",1000,DEBUG); // configure as access point
  
    sendCommand("AT+CWJAP=\"Speedy-AC6CA3\",\"matiasmanda\"\r\n",3000,DEBUG);
    //delay(20000);
    Serial.println(sendCommand("AT+CIFSR\r\n",1000,DEBUG)); // get ip address
    if(esp8266.find("STAIP,\"")){
      Serial.println("Entro al  IF");
      String response = "";
      for(int i=0; i<15; i++){
        char c = esp8266.read(); // read the next character.
        response+=c;
      }  
      
      Serial.println(response);
    }
    sendCommand("AT+CIPMUX=1\r\n",1000,DEBUG); // configure for multiple connections
    sendCommand("AT+CIPSERVER=1,80\r\n",1000,DEBUG); // turn on server on port 80
    Serial.println("Server Ready");

    servoMotor.attach(PinServo); // el servo trabajará desde el pin definido como PinServo
    servoMotor.write(ServoCerrado);   // Desplazamos a la posición 0
    tiempoInicialAmaque = millis();
    tiempoSilencio = millis();
    tiempoUltimoLlanto = millis();
}

void loop() {
  //amacarCuna();
  //escucharLlanto();
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
* Name: sendCommand
* Description: Function used to send data to ESP8266. Sirve para enviar los códigos AT
* Params: command - the data/command to send; timeout - the time to wait for a response; debug - print to Serial window?(true = yes, false = no)
* Returns: The response from the esp8266 (if there is a reponse)
*/
String sendCommand(String command, const int timeout, boolean debug)
{
    String response = "";
           
    esp8266.print(command); // send the read character to the esp8266
    
    long int time = millis();
    
    while( (time+timeout) > millis())
    {
      while(esp8266.available())
      {
        // The esp has data so display its output to the serial window 
        char c = esp8266.read(); // read the next character.
        response+=c;
      }  
    }
    
    if(debug)
    {
      Serial.print(response);
    }
    
    return response;
}
