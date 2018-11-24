#define  c3    7634
#define  d3    6803
#define  e3    6061
#define  f3    5714
#define  g3    5102
#define  a3    4545
#define  b3    4049
#define  c4    3816    // 261 Hz
#define  e4    3030    // 329 Hz
#define  f4    2865    // 349 Hz
#define  g4    2551    // 392 Hz
#define  a4    2272    // 440 Hz
#define  a4s   2146
#define  b4    2028    // 493 Hz
#define  c5    1912    // 523 Hz
#define  d5    1706
#define  d5s   1608
#define  e5    1517    // 659 Hz
#define  f5    1433    // 698 Hz
#define  g5    1276
#define  a5    1136
#define  a5s   1073
#define  b5    1012
#define  c6    955

#define NOTE_B0  31
#define NOTE_C1  33
#define NOTE_CS1 35
#define NOTE_D1  37
#define NOTE_DS1 39
#define NOTE_E1  41
#define NOTE_F1  44
#define NOTE_FS1 46
#define NOTE_G1  49
#define NOTE_GS1 52
#define NOTE_A1  55
#define NOTE_AS1 58
#define NOTE_B1  62
#define NOTE_C2  65
#define NOTE_CS2 69
#define NOTE_D2  73
#define NOTE_DS2 78
#define NOTE_E2  82
#define NOTE_F2  87
#define NOTE_FS2 93
#define NOTE_G2  98
#define NOTE_GS2 104
#define NOTE_A2  110
#define NOTE_AS2 117
#define NOTE_B2  123
#define NOTE_C3  131
#define NOTE_CS3 139
#define NOTE_D3  147
#define NOTE_DS3 156
#define NOTE_E3  165
#define NOTE_F3  175
#define NOTE_FS3 185
#define NOTE_G3  196
#define NOTE_GS3 208
#define NOTE_A3  220
#define NOTE_AS3 233
#define NOTE_B3  247
#define NOTE_C4  262
#define NOTE_CS4 277
#define NOTE_D4  294
#define NOTE_DS4 311
#define NOTE_E4  330
#define NOTE_F4  349
#define NOTE_FS4 370
#define NOTE_G4  392
#define NOTE_GS4 415
#define NOTE_A4  440
#define NOTE_AS4 466
#define NOTE_B4  494
#define NOTE_C5  523
#define NOTE_CS5 554
#define NOTE_D5  587
#define NOTE_DS5 622
#define NOTE_E5  659
#define NOTE_F5  698
#define NOTE_FS5 740
#define NOTE_G5  784
#define NOTE_GS5 831
#define NOTE_A5  880
#define NOTE_AS5 932
#define NOTE_B5  988
#define NOTE_C6  1047
#define NOTE_CS6 1109
#define NOTE_D6  1175
#define NOTE_DS6 1245
#define NOTE_E6  1319
#define NOTE_F6  1397
#define NOTE_FS6 1480
#define NOTE_G6  1568
#define NOTE_GS6 1661
#define NOTE_A6  1760
#define NOTE_AS6 1865
#define NOTE_B6  1976
#define NOTE_C7  2093
#define NOTE_CS7 2217
#define NOTE_D7  2349
#define NOTE_DS7 2489
#define NOTE_E7  2637
#define NOTE_F7  2794
#define NOTE_FS7 2960
#define NOTE_G7  3136
#define NOTE_GS7 3322
#define NOTE_A7  3520
#define NOTE_AS7 3729
#define NOTE_B7  3951
#define NOTE_C8  4186
#define NOTE_CS8 4435
#define NOTE_D8  4699
#define NOTE_DS8 4978

#define  R     0      // Define a special note, ‘R’, to represent a rest

// Frecuencias de las notas
#define DO4  262
#define RE4  294
#define MI4  330
#define FA4  349
#define FAS4  370
#define SOL4  392
#define LA4  440
#define LAS4  466
#define SI4  494
#define DO5  523
#define RE5  587

// Melody 1: Star Wars Imperial March

int melody1[] = {  a4, R,  a4, R,  a4, R,  f4, R, c5, R,  a4, R,  f4, R, c5, R, a4, R,  e5, R,  e5, R,  e5, R,  f5, R, c5, R,  g5, R,  f5, R,  c5, R, a4, R};
int beats1[]  = {  50, 20, 50, 20, 50, 20, 40, 5, 20, 5,  60, 10, 40, 5, 20, 5, 60, 80, 50, 20, 50, 20, 50, 20, 40, 5, 20, 5,  60, 10, 40, 5,  20, 5, 60, 40};

// Melody 2: Star Wars Theme
int melody2[] = {  f4,  f4, f4,  a4s,   f5,  d5s,  d5,  c5, a5s, f5, d5s,  d5,  c5, a5s, f5, d5s, d5, d5s,   c5};
int beats2[]  = {  21,  21, 21,  128,  128,   21,  21,  21, 128, 64,  21,  21,  21, 128, 64,  21, 21,  21, 128 };

// Melody 3 : Mario
int melody3[] = {
  NOTE_E7, NOTE_E7, 0, NOTE_E7,
  0, NOTE_C7, NOTE_E7, 0,
  NOTE_G7, 0, 0,  0,
  NOTE_G6, 0, 0, 0,
 
  NOTE_C7, 0, 0, NOTE_G6,
  0, 0, NOTE_E6, 0,
  0, NOTE_A6, 0, NOTE_B6,
  0, NOTE_AS6, NOTE_A6, 0,
 
  NOTE_G6, NOTE_E7, NOTE_G7,
  NOTE_A7, 0, NOTE_F7, NOTE_G7,
  0, NOTE_E7, 0, NOTE_C7,
  NOTE_D7, NOTE_B6, 0, 0,
 
  NOTE_C7, 0, 0, NOTE_G6,
  0, 0, NOTE_E6, 0,
  0, NOTE_A6, 0, NOTE_B6,
  0, NOTE_AS6, NOTE_A6, 0,
 
  NOTE_G6, NOTE_E7, NOTE_G7,
  NOTE_A7, 0, NOTE_F7, NOTE_G7,
  0, NOTE_E7, 0, NOTE_C7,
  NOTE_D7, NOTE_B6, 0, 0
};
int beats3[]  = {
  12, 12, 12, 12,
  12, 12, 12, 12,
  12, 12, 12, 12,
  12, 12, 12, 12,
 
  12, 12, 12, 12,
  12, 12, 12, 12,
  12, 12, 12, 12,
  12, 12, 12, 12,
 
  9, 9, 9,
  12, 12, 12, 12,
  12, 12, 12, 12,
  12, 12, 12, 12,
 
  12, 12, 12, 12,
  12, 12, 12, 12,
  12, 12, 12, 12,
  12, 12, 12, 12,
 
  9, 9, 9,
  12, 12, 12, 12,
  12, 12, 12, 12,
  12, 12, 12, 12,
};

// Melody 4 underworld_melody
int melody4[] = {
  NOTE_C4, NOTE_C5, NOTE_A3, NOTE_A4,
  NOTE_AS3, NOTE_AS4, 0,
  0,
  NOTE_C4, NOTE_C5, NOTE_A3, NOTE_A4,
  NOTE_AS3, NOTE_AS4, 0,
  0,
  NOTE_F3, NOTE_F4, NOTE_D3, NOTE_D4,
  NOTE_DS3, NOTE_DS4, 0,
  0,
  NOTE_F3, NOTE_F4, NOTE_D3, NOTE_D4,
  NOTE_DS3, NOTE_DS4, 0,
  0, NOTE_DS4, NOTE_CS4, NOTE_D4,
  NOTE_CS4, NOTE_DS4,
  NOTE_DS4, NOTE_GS3,
  NOTE_G3, NOTE_CS4,
  NOTE_C4, NOTE_FS4, NOTE_F4, NOTE_E3, NOTE_AS4, NOTE_A4,
  NOTE_GS4, NOTE_DS4, NOTE_B3,
  NOTE_AS3, NOTE_A3, NOTE_GS3,
  0, 0, 0
};
int beats4[] = {
  12, 12, 12, 12,
  12, 12, 6,
  3,
  12, 12, 12, 12,
  12, 12, 6,
  3,
  12, 12, 12, 12,
  12, 12, 6,
  3,
  12, 12, 12, 12,
  12, 12, 6,
  6, 18, 18, 18,
  6, 6,
  6, 6,
  6, 6,
  18, 18, 18, 18, 18, 18,
  10, 10, 10,
  10, 10, 10,
  3, 3, 3
};


// Melodia 5 : navidad
int melody5[] = {
  DO4, FA4, FA4, SOL4, FA4, MI4, RE4, RE4, RE4, SOL4, SOL4, LA4, SOL4, FA4, MI4, DO4, DO4,
  LA4, LA4, LAS4, LA4, SOL4, FA4, RE4, DO4, DO4, RE4, SOL4, MI4, FA4, DO4, FA4, FA4, FA4,
  MI4, MI4, FA4, MI4, RE4, DO4, SOL4, LA4, SOL4, SOL4, FA4, FA4, DO5, DO4, DO4, DO4,
  RE4, SOL4, MI4, FA4
};
int beats5[] = {
  2, 2, 3, 3, 3, 3, 2, 2, 2, 2, 3, 3, 3, 3, 2, 2, 2,
  2, 3, 3, 3, 3, 2, 2, 3, 3, 2, 2, 2, 1, 2, 2, 2, 2,
  1, 2, 2, 2, 2, 1, 2, 2, 3, 3, 3, 3, 2, 2, 3, 3,
  2, 2, 2, 1
};

int MAX_COUNT = sizeof(melody1) / 2;
long tempo = 10000;
int pause = 1000;
int rest_count = 50;
int toneM = 0;
int beat = 0;
long duration  = 0;
int potVal = 0;
int speakerOut;

int tiempoInicialTone = 0;
int tiempoActualTone = 0;

void iniciarMelodia(int speakerOut1){
  speakerOut = speakerOut1;
  pinMode(speakerOut, OUTPUT);

}



void playTone() {

long elapsed_time = 0;
if (toneM > 0) {

while (elapsed_time < duration) {
digitalWrite(speakerOut,HIGH);
delayMicroseconds(toneM / 2);
digitalWrite(speakerOut, LOW);
delayMicroseconds(toneM / 2);
elapsed_time += (toneM);

       }
}else {
  for (int j = 0; j < rest_count; j++) {
   delayMicroseconds(duration);
  }
}

}

void sonarMelody1(){
  tiempoInicialTone = millis();
  for (int i=0; i<MAX_COUNT; i++) {
    toneM = melody1[i];
    beat = beats1[i];
    duration = beat * tempo;
    playTone();
    delayMicroseconds(pause);
  }
}

void sonarMelody2(){
  MAX_COUNT = sizeof(melody2) / 2;
  for (int i = 0; i < MAX_COUNT; i++) {
    toneM = melody2[i];
    beat = beats2[i];
    duration = beat * tempo;
    playTone();
    delayMicroseconds(pause);
  }
}

void sonarMelody3(){
  MAX_COUNT = sizeof(melody2) / 2;
  for (int i = 0; i < MAX_COUNT; i++) {
    toneM = melody3[i];
    beat = beats3[i];
    duration = beat * tempo;
    playTone();
    delayMicroseconds(pause);
  }
}



void sonarMelody4(){
  MAX_COUNT = sizeof(melody2) / 2;
  for (int i = 0; i < MAX_COUNT; i++) {
    toneM = melody4[i];
    beat = beats4[i];
    duration = beat * tempo;
    playTone();
    delayMicroseconds(pause);
  }
}

void sonarMelody5(){
  MAX_COUNT = sizeof(melody2) / 2;
  for (int i = 0; i < MAX_COUNT; i++) {
    toneM = melody5[i];
    beat = beats5[i];
    duration = beat * tempo;
    playTone();
    delayMicroseconds(pause);
  }
}


int length = 15; // the number of notes6

//twinkle twinkle little star
char notes6[] = "ccggaag ffeeddc ggffeed ggffeed ccggaag ffeeddc "; // a space represents a rest
int beats6[] = { 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 2, 4 };
int tempo6 = 300;


void playTono(int tone, int duration) {
  for (long i = 0; i < duration * 1000L; i += tone * 2) {
    digitalWrite(speakerOut, HIGH);
    delayMicroseconds(tone);
    digitalWrite(speakerOut, LOW);
    delayMicroseconds(tone);
  }
}

void playNota(char note, int duration) {
  char names[] = { 'c', 'd', 'e', 'f', 'g', 'a', 'b', 'C' };
  int tones[] = { 1915, 1700, 1519, 1432, 1275, 1136, 1014, 956 };
  
  // play the tone corresponding to the note name
  for (int i = 0; i < 8; i++) {
    if (names[i] == note) {
      playTono(tones[i], duration);
    }
  }
}



void sonarMelody6(){
    for (int i = 0; i < length; i++) {
    if (notes6[i] == ' ') {
      delay(beats6[i] * tempo6); // rest
    } else {
      playNota(notes6[i], beats6[i] * tempo6);
    }
    
    // pause between notes6
    delay(tempo6 / 2); 
  }
}
void apagarMelody(){
  digitalWrite(speakerOut,LOW);
}
