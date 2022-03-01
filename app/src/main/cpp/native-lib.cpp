#include <jni.h>
#include <string>
typedef unsigned char uchar;

extern "C"
JNIEXPORT jfloat JNICALL
Java_sample_nfc_reader_MainActivity_decodeNfc(JNIEnv *env, jobject thiz, jbyteArray block) {
    uchar result[16];
    env -> GetByteArrayRegion(block, 0, 16, (jbyte*) result);

    uchar G1 = result[0], G2 = result[1];
    G2 += G1 >> 8;

    return float((G2 << 8) + G1) / 100.0F;
}
