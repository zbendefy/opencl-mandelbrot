#if defined( USE_HIGH_PRECISION )
    #pragma OPENCL EXTENSION cl_khr_fp64 : enable
    typedef double RealNumber;
#else
    typedef float RealNumber;
#endif

typedef struct Complex {   
	 RealNumber r;   
	 RealNumber i;   
 } Complex; 

 inline Complex cMul(Complex c1, Complex c2)
 {
 	Complex ret;
 	ret.r = c1.r * c2.r - c1.i * c2.i;
 	ret.i = c1.r * c2.i + c1.i * c2.r;
 	return ret;
 }
 
 inline RealNumber cLength(Complex c)
 {
 	return sqrt(c.r*c.r+c.i*c.i);
 }
 
 Complex cPow(Complex c1, int exp)
 {
 	Complex ret = c1;
 	for(int i = 1; i < exp; ++i)
 	{
 		ret = cMul(ret, c1);
 	}
 	return ret;
 }
  
 //intParams[0,1,2] = 0:iteration limit, 1: image width, 2: image height 
 //realParams[0,1,2,3] = 0: pan x, 1: pan y, 2: zoom factor, 3: aspectRatio
__kernel void mandelbrot(__constant const int *intParams, __constant const RealNumber *realParams, __global __write_only char *output) 
{ 
	if (get_global_id(0) >= intParams[1] || get_global_id(1) >= intParams[2])
		return; 
		
	Complex C; 
	Complex previous; 
	 
	//coordinates in -1 to +1 
	const RealNumber screenx = (( ((RealNumber)get_global_id(0)) / ((RealNumber)(intParams[1]-1))) - 0.5f) * 2.0f; 
	const RealNumber screeny = (( ((RealNumber)get_global_id(1)) / ((RealNumber)(intParams[2]-1))) - 0.5f) * 2.0f; 
	 
	const uint imgid = get_global_id(1) * intParams[1] + get_global_id(0); 
	 
	C.r = realParams[0] + realParams[2] * screenx; //  x 
	C.i = realParams[1] + (realParams[2] * screeny) * realParams[3]; //  y 
	 
	previous.r = 0; 
	previous.i = 0; 
	 
	const uint iterationLimit = intParams[0]+1;
	 
	for (uint i = 0; i < iterationLimit; ++i) 
	{ 
		const RealNumber x2 = previous.r * previous.r;
		const RealNumber y2 = previous.i * previous.i;
		 
		if (x2 + y2 > 4)  //spares the sqrt function by using 4 instead of 2
		{ 
			output[imgid] = ((i * 155) / iterationLimit) - 28;
			return;
		} 
		
		previous.i = (previous.i * previous.r * 2) + C.i;
		previous.r = (x2 - y2) + C.r; 
	} 
	 
	output[imgid] = -128;
}

//intParams[0,1,2] = 0:iteration limit, 1: image width, 2: image height 
 //realParams[0,1,2,3] = 0: pan x, 1: pan y, 2: zoom factor, 3: aspectRatio, 4-5: julia parameters
__kernel void julia(__constant const int *intParams, __constant const RealNumber *realParams, __global __write_only char *output) 
{ 
	if (get_global_id(0) >= intParams[1] || get_global_id(1) >= intParams[2])
		return; 
		
	Complex C; 
	Complex previous; 
	 
	//coordinates in -1 to +1 
	const RealNumber screenx = (( ((RealNumber)get_global_id(0)) / ((RealNumber)(intParams[1]-1))) - 0.5f) * 2.0f; 
	const RealNumber screeny = (( ((RealNumber)get_global_id(1)) / ((RealNumber)(intParams[2]-1))) - 0.5f) * 2.0f; 
	 
	const uint imgid = get_global_id(1) * intParams[1] + get_global_id(0); 
	 
	C.r = realParams[4]; 
	C.i = realParams[5];
	 
	previous.r = realParams[0] + realParams[2] * screenx; 
	previous.i = realParams[1] + (realParams[2] * screeny) * realParams[3]; 
	 
	const uint iterationLimit = intParams[0]+1;
	 
	for (uint i = 0; i < iterationLimit; ++i) 
	{ 
		const RealNumber x2 = previous.r * previous.r;
		const RealNumber y2 = previous.i * previous.i;
		 
		if (x2 + y2 > 4)  //spares the sqrt function by using 4 instead of 2
		{ 
			output[imgid] = ((i * 155) / iterationLimit) - 28;
			return;
		} 
		
		previous.i = (previous.i * previous.r * 2) + C.i;
		previous.r = (x2 - y2) + C.r; 
	} 
	 
	output[imgid] = -128;
}


 //intParams[0,1,2] = 0:iteration limit, 1: image width, 2: image height, 3: exponent
 //realParams[0,1,2,3] = 0: pan x, 1: pan y, 2: zoom factor, 3: aspectRatio
__kernel void mandelbrotN(__constant const int *intParams, __constant const RealNumber *realParams, __global __write_only char *output) 
{ 
	if (get_global_id(0) >= intParams[1] || get_global_id(1) >= intParams[2])
		return; 
		
	Complex C; 
	Complex previous; 
	 
	//coordinates in -1 to +1 
	const RealNumber screenx = (( ((RealNumber)get_global_id(0)) / ((RealNumber)(intParams[1]-1))) - 0.5f) * 2.0f; 
	const RealNumber screeny = (( ((RealNumber)get_global_id(1)) / ((RealNumber)(intParams[2]-1))) - 0.5f) * 2.0f; 
	 
	const uint imgid = get_global_id(1) * intParams[1] + get_global_id(0); 
	 
	C.r = realParams[0] + realParams[2] * screenx; //  x 
	C.i = realParams[1] + (realParams[2] * screeny) * realParams[3]; //  y 
	 
	previous.r = 0; 
	previous.i = 0; 
	 
	const uint iterationLimit = intParams[0]+1;
	const int exponent = intParams[3];
	 
	for (uint i = 0; i < iterationLimit; ++i) 
	{ 
		Complex z_n; 
		z_n = cPow(previous, exponent);
		
		previous.r = z_n.r + C.r;
		previous.i = z_n.i + C.i;
	
		if (previous.r * previous.r + previous.i * previous.i > 4)  //spares the sqrt function by using 4 instead of 2
		{ 
			output[imgid] = ((i * 155) / iterationLimit) - 28;
			return;
		} 
	} 
	 
	output[imgid] = -128;
}

//intParams[0,1,2] = 0:iteration limit, 1: image width, 2: image height, 3: exponent
 //realParams[0,1,2,3] = 0: pan x, 1: pan y, 2: zoom factor, 3: aspectRatio, 4-5: julia parameters
__kernel void juliaN(__constant const int *intParams, __constant const RealNumber *realParams, __global __write_only char *output) 
{ 
	if (get_global_id(0) >= intParams[1] || get_global_id(1) >= intParams[2])
		return; 
		
	Complex C; 
	Complex previous; 
	 
	//coordinates in -1 to +1 
	const RealNumber screenx = (( ((RealNumber)get_global_id(0)) / ((RealNumber)(intParams[1]-1))) - 0.5f) * 2.0f; 
	const RealNumber screeny = (( ((RealNumber)get_global_id(1)) / ((RealNumber)(intParams[2]-1))) - 0.5f) * 2.0f; 
	 
	const uint imgid = get_global_id(1) * intParams[1] + get_global_id(0); 
	 
	C.r = realParams[4]; 
	C.i = realParams[5];
	 
	previous.r = realParams[0] + realParams[2] * screenx; 
	previous.i = realParams[1] + (realParams[2] * screeny) * realParams[3]; 
	 
	const uint iterationLimit = intParams[0]+1;
	const int exponent = intParams[3];
	 
	for (uint i = 0; i < iterationLimit; ++i) 
	{ 
		Complex z_n = cPow(previous, exponent);
		
		previous.r = z_n.r + C.r;
		previous.i = z_n.i + C.i;
	
		if (previous.r * previous.r + previous.i * previous.i > 4)  //spares the sqrt function by using 4 instead of 2
		{ 
			output[imgid] = ((i * 155) / iterationLimit) - 28;
			return;
		} 
	} 
	 
	output[imgid] = -128;
}