#include <ArduinoJson.h>

//Fuente https://programarfacil.com/tutoriales/fragmentos/servomotor-con-arduino/
#include <Servo.h>
#define ESPERA_LECTURAS 2000 // tiempo en milisegundos entre lecturas de la intensidad de la luz
//Variables del Servo
#define PinServo      9   //Pin donde está conectado el servo 
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
#define PinLED 11 // Pin donde esta conectado el LED (PWM)
long cronometro_lecturas=0;
long tiempo_transcurrido;
unsigned int luminosidad;
float coeficiente_porcentaje=100.0/1023.0; // El valor de la entrada analógica va de 0 a 1023 y se quiere convertir a porcentaje que va de cero a 100

#include <SoftwareSerial.h>
#include <SPI.h>
#include <Ethernet.h>
 // arduino Rx (pin 2) ---- ESP8266 Tx
 // arduino Tx (pin 3) ---- ESP8266 Rx
SoftwareSerial esp8266(2,3); 
int tiempoInicioWIFI = 0;
int tiempoWIFI = 0;
#define DEBUG true
int conexionID = 0;
String peticion = "";



DynamicJsonBuffer jsonBuffer;
void setup()
{
  Serial.begin(115200);  // monitor serial del arduino
  esp8266.begin(115200); // baud rate del ESP8255

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
  
  sendCommand("AT+RST\r\n",2000,DEBUG);      // resetear módulo
  sendCommand("AT+CWSAP=\"ARDUINO\",\"soa-criard-266\",3,2\r\n",8000,DEBUG); 
  sendCommand("AT+CWMODE=2\r\n",1000,DEBUG); // configurar como cliente
  //sendCommand("AT+CWJAP=\"Speedy-AC6CA3\",\"matiasmanda\"\r\n",8000); //SSID y contraseña para unirse a red 
  sendCommand("AT+CIFSR\r\n",1000,DEBUG);    // obtener dirección IP
  sendCommand("AT+CIPMUX=1\r\n",1000,DEBUG); // configurar para multiples conexiones
  sendCommand("AT+CIPSERVER=1,80\r\n",1000,DEBUG);         // servidor en el puerto 80
  
}


void loop()
{
  if(esp8266.available()){
      detectarCliente();
  }
  amacarCuna();
}
bool detectarCliente(){

 //tiempoWIFI = millis() - tiempoInicioWIFI;
         
 //if(tiempoWIFI > 1500){
 //Serial.println("Paso tiempo de espera...");
  
  if(esp8266.find("+IPD,")) // revisar si el servidor recibio datos
     {
        
           //delay(1500); // esperar que lleguen los datos hacia el buffer
           conexionID = esp8266.read()-48; // obtener el ID de la conexión para poder responder
           int state = analizarPeticion();
           String respuesta = construirRespuesta(state);
           enviarRespuesta(respuesta);
           tiempoInicioWIFI = millis();
           
     }   
     
  
  //}
}
int analizarPeticion(){

     int state = 0; 
     //Formato URL = http://192.168.4.1/led=1
     if(esp8266.find("servo=")!= -1){

        state = (esp8266.read()-48); // Obtener el estado del pin a mostrar
        Serial.print("Estado recibido: ");
        Serial.println(esp8266.read());
        Serial.print("Estado del state ");
        Serial.println(state);
        if(state==1){
          prendoCuna = 1;  
        }else{
          prendoCuna = 0; 
        }
        
      } else{
          prendoCuna = 0; 
        }
    return state;
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
Funciones de ESP8266
**/
String sendData(String command, const int timeout, boolean debug)
{
    String response = "";
    
    int dataSize = command.length();
    char data[dataSize];
    command.toCharArray(data,dataSize);
           
    esp8266.write(data,dataSize); // send the read character to the esp8266
    if(debug)
    {
      Serial.println("\r\n====== HTTP Response From Arduino ======");
      Serial.write(data,dataSize);
      Serial.println("\r\n========================================");
    }
    
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
 

void sendHTTPResponse(int connectionId, String content)
{
     // build HTTP response
     String httpResponse;
     String httpHeader;
     // HTTP Header
     httpHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n"; 
     httpHeader += "Content-Length: ";
     httpHeader += content.length();
     httpHeader += "\r\n";
     httpHeader +="Connection: close\r\n\r\n";
     httpResponse = httpHeader + content + " "; // There is a bug in this code: the last character of "content" is not sent, I cheated by adding this extra space
     //for(int i=0; i<= connectionId; i++){
     sendCIPData(connectionId,httpResponse);
      //delay(1500);
     //}
}
 

void sendCIPData(int connectionId, String data)
{
   String cipSend = "AT+CIPSEND=";
   cipSend += connectionId;
   cipSend += ",";
   cipSend +=data.length();
   cipSend +="\r\n";
   sendCommand(cipSend,2000,DEBUG);
   sendData(data,2200,DEBUG);
}
 
String sendCommand(String command, const int timeout, boolean debug)
{
    String response = "";          
    esp8266.print(command); // send the read character to the esp8266   
    long int time = millis();   
    while( (time+timeout) > millis()){
      while(esp8266.available()){
        
        // The esp has data so display its output to the serial window 
        char c = esp8266.read(); // read the next character.
        response+=c;
      }  
    }  
    if(debug){
      Serial.print(response);
    }   
    return response;
}



String construirRespuesta(int state){
  String output;
  String strState =  String(state);
  String input = "{\"led\":\""+strState+"\", \"humedad\":\"80%\"}";
  JsonObject& root = jsonBuffer.parseObject(input);
        //responder y cerrar la conexión para que el navegador no se quede cargando 
        // página web a enviar
        String webpage = "";
        if (state==1){
          root[String("led")] = 1;
          //webpage += "Led = encendido!";
        }
        else { webpage += "Led = apagado!";}
        
       return input;
       
  }
bool enviarRespuesta(String respuesta){
      sendHTTPResponse(conexionID,respuesta);
  }
  
bool cerrarConexion(int connectionId){
  
       // comando para terminar conexión
       String comandoCerrar = "AT+CIPCLOSE=";
       comandoCerrar+=connectionId;
       comandoCerrar+="\r\n";
       sendData(comandoCerrar,3000,DEBUG);
  }
void resetWIFI(){
    sendCommand("AT+RST\r\n",2000,DEBUG);      // resetear módulo
    sendCommand("AT+CWSAP=\"ARDUINO\",\"soa-criard-266\",3,2\r\n",8000,DEBUG); 
    sendCommand("AT+CWMODE=2\r\n",2000,DEBUG); // configurar como cliente
    //sendCommand("AT+CWJAP=\"Speedy-AC6CA3\",\"matiasmanda\"\r\n",8000); //SSID y contraseña para unirse a red 
    sendCommand("AT+CIFSR\r\n",2000,DEBUG);    // obtener dirección IP
    sendCommand("AT+CIPMUX=1\r\n",2000,DEBUG); // configurar para multiples conexiones
    sendCommand("AT+CIPSERVER=1,80\r\n",2000,DEBUG);         // servidor en el puerto 80
  
  }
