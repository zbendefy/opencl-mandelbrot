%64bit_pragma%

typedef %numberformat% RealNumber;

typedef struct Complex {   
	 RealNumber r;   
	 RealNumber i;   
 } Complex; 
 
 #define LIMIT 4

 //intParams[0,1,2] = 0:iteration limit, 1: image width, 2: image height 
 //realParams[0,1,2,3] = 0: pan x, 1: pan y, 2: zoom factor, 3: aspectRatio
__kernel void mandelbrot(__global __read_only const int *intParams, __global __read_only const RealNumber *realParams, __global __write_only char *output) 
{ 
	Complex C; 
	Complex sum; 
	Complex previous; 
	 
	//coordinates in -1 to +1 
	const RealNumber screenx = (( ((RealNumber)get_global_id(0)) / ((RealNumber)(intParams[1]-1))) - 0.5f) * 2.0f; 
	const RealNumber screeny = (( ((RealNumber)get_global_id(1)) / ((RealNumber)(intParams[2]-1))) - 0.5f) * 2.0f; 
	 
	const uint imgid = get_global_id(1) * intParams[1] + get_global_id(0); 
	 
	C.r = realParams[0] + realParams[2] * screenx; //  x 
	C.i = realParams[1] + (realParams[2] * screeny) * realParams[3]; //  y 
	 
	sum.r = 0; //  x 
	sum.i = 0; //  y 
	
	previous.r = 0; 
	previous.i = 0; 
	 
	const uint iterationLimit = intParams[0]+1;
	 
	for (uint i = 0; i < iterationLimit; i++) 
	{ 
		const RealNumber x2 = previous.r * previous.r;
		const RealNumber y2 = previous.i * previous.i;
		 
		if (x2 + y2 > 4)  //spares the sqrt function by using 4 instead of 2
		{ 
			output[imgid] = ((i * 155) / iterationLimit) - 28;
			return;
		} 
		
		sum.r = (x2 - y2) + C.r; 
		sum.i = (previous.i * previous.r * 2) + C.i; 
		 
		previous.r = sum.r; 
		previous.i = sum.i; 
	} 
	 
	output[imgid] = -128;
}