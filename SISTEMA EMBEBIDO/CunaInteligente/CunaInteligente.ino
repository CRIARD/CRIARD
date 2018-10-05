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


void setup() {
  Serial.begin (9600);
  servoMotor.attach(PinServo); // el servo trabajará desde el pin definido como PinServo
  servoMotor.write(ServoCerrado);   // Desplazamos a la posición 0
  tiempoInicialAmaque = millis();
  tiempoSilencio = millis();
  tiempoUltimoLlanto = millis();
}

void loop() {
  amacarCuna();
  escucharLlanto();


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


