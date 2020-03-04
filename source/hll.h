#pragma once
#include "stdafx.h"

#ifdef HLLCLIENTLIBRARY_EXPORTS
#define HLLCLIENTLIBRARY_API __declspec(dllexport)
#else
#define HLLCLIENTLIBRARY_API __declspec(dllimport)
#endif

extern "C" HLLCLIENTLIBRARY_API long WINAPI hllapi(LPINT functionNum, LPSTR dataString, LPINT length, LPINT psPosition);
extern "C" HLLCLIENTLIBRARY_API long WINAPI hllapiw(LPINT functionNum, LPWSTR dataString, LPINT length, LPINT psPosition);

extern "C" HLLCLIENTLIBRARY_API long WINAPI hllapi_read(LPINT functionNum, LPSTR dataString, LPINT length, LPINT psPosition);
extern "C" HLLCLIENTLIBRARY_API long WINAPI hllapi_write(LPINT functionNum, LPSTR dataString, LPINT length, LPINT psPosition);

extern "C" HLLCLIENTLIBRARY_API long WINAPI hllapiw_read(LPINT functionNum, LPWSTR dataString, LPINT length, LPINT psPosition);
extern "C" HLLCLIENTLIBRARY_API long WINAPI hllapiw_write(LPINT functionNum, LPWSTR dataString, LPINT length, LPINT psPosition);

extern "C" HLLCLIENTLIBRARY_API const char* WINAPI getInfo();
extern "C" HLLCLIENTLIBRARY_API const char* WINAPI getVersion();
