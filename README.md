# opencl-mandelbrot
Mandelbrot viewer written in OpenCL

Written in java, and OpenCL (using the JOCL 1.9 library), therefore an installed OpenCL driver is required.
AMD's OpenCL driver can provide OpenCL support for both Linux and Windows, on any CPU that supports SSE2.

Features:
-Mandelbrot sets (z -> z^n + c) with exponents (n) from 2 to 16
-Julia sets at any position and exponents from 2 to 16
-64-bit precision for devices that support it
-Iteration level from 1 to 3600

Usage:
Use the arrow keys to move, and PgUp, PgDn to zoom.

Tested on linux and windows, should work on mac too.
