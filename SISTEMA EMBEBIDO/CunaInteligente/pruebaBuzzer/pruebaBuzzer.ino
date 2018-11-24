#include "melodias.h"
int flagBuzzer=0;
void setup() {
 iniciarMelodia(12);
 Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
       Serial.println("loop");
        if(flagBuzzer==0){
         Serial.print("musica");
          //sonarMelody3();
         //sonarMelody4();
         sonarMelody6();
          }
        flagBuzzer=1;
}
