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
#define pinMicro      A5   //Pin donde está conectado el microfono
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


void setup() {
  Serial.begin (9600);
  servoMotor.attach(PinServo); // el servo trabajará desde el pin definido como PinServo
  servoMotor.write(ServoCerrado);   // Desplazamos a la posición 0

  pinMode(PinLDR,INPUT); //Defino el tipo de pin para el LDR (Entrada)
  pinMode(PinLED,OUTPUT); //Defino el tipo de pin para el LED (Salida)
  
  tiempoInicialAmaque = millis();
  tiempoSilencio = millis();
  tiempoUltimoLlanto = millis();
  tiempoInicioProm = millis();
  tiempoFinProm = 0;
}

void loop() {
  
  //****Estas funciones permiten activar el servo en relacion al sonido detectado por el microfono****//
  
  //ruidoAmbiente();
  //amacarCuna();
  //escucharLlanto();

  //****Estas funciones permiten accionar el led de manera gradual conforme aumenta o disminuye la luz ambiente****//
  
  //float luz= detectarLuz();//detecto la luz ambiente
  //encenderLEDGradual(luz); //Enciendo el led gradualmente segun la luz ambiente.

}

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

void ruidoAmbiente(){
 
    int lecturaRuido = analogRead(pinMicro);
  
    if(tiempoFinProm > ESPERA_LECTURAS){
      
      muestras += lecturaRuido;
      Serial.print("Valor Medido: ");
      Serial.print(lecturaRuido);
      Serial.print("Valor Acumulado: ");
      Serial.println(muestras);
      
      }
       
    tiempoFinProm = millis() - tiempoInicioProm;
 
 
  }
