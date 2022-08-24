//
// Created by binsheng.zhu on 2022/8/23.
//
#include <jni.h>
#include <GLES3/gl3.h>

#include <android/log.h>

#define LIBENC_LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "libglutil", __VA_ARGS__))
#define LIBENC_LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO , "libglutil", __VA_ARGS__))
#define LIBENC_LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN , "libglutil", __VA_ARGS__))
#define LIBENC_LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "libglutil", __VA_ARGS__))

#define LIBENC_ARRAY_ELEMS(a)  (sizeof(a) / sizeof(a[0]))

static JavaVM *gjvm= NULL;
static jobject gClassLoader = NULL;
static jmethodID gFindClassMethod = NULL;

static JNIEnv *gjenv= NULL;

static jclass gjClass_UnityConnect   = NULL;
static jmethodID gjMethodId_sendRgbaFrame = NULL;

JNIEnv* getEnv();
jclass findClass(JNIEnv *env, const char* name);

extern "C" {
using UnityRenderEvent = void(*)();

UnityRenderEvent GetRenderEventFunc();
}

// call from Unity(IssuePluginEvent)
void nativeRender()
{
    int eventId;
    LIBENC_LOGD("onRenderEvent:%d", eventId);
    JNIEnv *jenv = getEnv();
    if (gjClass_UnityConnect == NULL)
    {
        gjClass_UnityConnect = (jclass)jenv->NewGlobalRef(findClass(jenv, "com/bl/unityhook/GLTexture"));
        gjMethodId_sendRgbaFrame = jenv->GetStaticMethodID(gjClass_UnityConnect, "initEglContext", "(I)V");
    }
    if (gjClass_UnityConnect != NULL)
    {
        jenv->CallStaticVoidMethod(gjClass_UnityConnect, gjMethodId_sendRgbaFrame, eventId);
        jthrowable mException = jenv->ExceptionOccurred();
        if (mException)
        {
            jenv->ExceptionDescribe();
            jenv->ExceptionClear();
        }
    }
    else
    {
        LIBENC_LOGE("jClass not got");
    }
}

// call from Unity(IssuePluginEvent)
UnityRenderEvent GetRenderEventFunc()
{
    return nativeRender;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    gjvm = vm;

    if ((gjenv = getEnv()) == NULL)
    {
        return JNI_ERR;
    }

    auto randomClass = gjenv->FindClass("com/bl/unityhook/GLTexture");
    jclass classClass = gjenv->GetObjectClass(randomClass);
    auto classLoaderClass = gjenv->FindClass("java/lang/ClassLoader");
    auto getClassLoaderMethod = gjenv->GetMethodID(classClass, "getClassLoader", "()Ljava/lang/ClassLoader;");
    gClassLoader = gjenv->NewGlobalRef(gjenv->CallObjectMethod(randomClass, getClassLoaderMethod));
    gFindClassMethod = gjenv->GetMethodID(classLoaderClass, "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");

    LIBENC_LOGD("JNI_OnLoad");
    return JNI_VERSION_1_6;
}

JNIEnv* getEnv()
{
    JNIEnv *env;
    int status = gjvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if(status < 0)
    {
        status = gjvm->AttachCurrentThread(&env, NULL);
        if(status < 0)
        {
            LIBENC_LOGE("Env not got");
            return NULL;
        }
    }
    return env;
}

jclass findClass(JNIEnv *env, const char* name)
{
    jclass resultClass = env->FindClass(name);
    jthrowable mException = env->ExceptionOccurred();
    if (mException)
    {
        //env->ExceptionDescribe();
        env->ExceptionClear();
        return static_cast<jclass>(env->CallObjectMethod(gClassLoader, gFindClassMethod, env->NewStringUTF(name)));
    }
    return resultClass;
}