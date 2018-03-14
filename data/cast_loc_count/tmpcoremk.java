public class CoreListJoinA {
	public static class ListData {
		public short data16;
		public short idx;	}
	public static class ListHead {
		ListHead next;
		ListData info;	}
	private static abstract class AbstractListDataCompare {
		abstract int compare(ListData a, ListData b, CoreResults res);	}
	private static class CmpComplex extends AbstractListDataCompare {
		@Lightweight(rank=4) // Needs to come after crc
		static short calc_func(ListData pdata, CoreResults res) {
			short data=pdata.data16;
			short retval;
			byte optype=(byte)((data>>7) & 1); /* bit 7 indicates if the function result has been cached */
			if (optype!=0) /* if cached, use cache */
				return (short)(data & 0x007f);
			else { /* otherwise calculate and cache the result */
				short flag=(short)(data & 0x7); /* bits 0-2 is type of function to perform */
				short dtype=(short)((data>>3) & 0xf); /* bits 3-6 is specific data for the operation */
				dtype |= dtype << 4; /* replicate the lower 4 bits to get an 8b value */
				switch (flag) {
					case 0:
						if (dtype<0x22) /* set min period for bit corruption */
							dtype=0x22;
						retval=CoreState.core_bench_state(res.size,res.statememblock3,res.seed1,res.seed2,dtype,res.crc);
						if (res.crcstate==0)
							res.crcstate=retval;
						break;
					case 1:
						retval=CoreMatrix.core_bench_matrix(res.mat,dtype,res.crc);
						if (res.crcmatrix==0)
							res.crcmatrix=retval;
						break;
					default:
						retval=data;
						break;				}
				res.crc=CoreUtil.crcu16(retval,res.crc);
				retval &= 0x007f; 
				pdata.data16=(short)((data & 0xff00) | 0x0080 | retval); /* cache the result */
				return retval;			}		}
		public int compare(ListData a, ListData b, CoreResults res) {
			short val1=calc_func(a,res);
			short val2=calc_func(b,res);
			return val1 - val2;		}	}
	private static CmpComplex cmp_complex = new CmpComplex();
	private static class CmpIdx extends AbstractListDataCompare {
		public int compare(ListData a, ListData b, CoreResults res) {
			if (res==null) {
				a.data16=(short)((a.data16 & 0xff00) | (0x00ff & (a.data16>>8)));
				b.data16=(short)((b.data16 & 0xff00) | (0x00ff & (b.data16>>8)));			}
			return a.idx - b.idx;		}	}
	private static CmpIdx cmp_idx = new CmpIdx();
	static short core_bench_list(CoreResults res, short finder_idx) {
		short retval=0, found=0,missed=0,find_num=res.seed3, i;
		ListHead list=res.list_CoreListJoinA,this_find, finder, remover;
		ListData info=new ListData();
		info.idx=finder_idx;
		for (i=0; i<find_num; i++) {
			info.data16=(short)(i & 0xff);
			this_find=core_list_find(list,info);
			list=core_list_reverse(list);
			if (this_find==null) {
				missed++;
				retval+=(list.next.info.data16 >> 8) & 1;			}
			else {
				found++;
				if ((this_find.info.data16 & 0x1) != 0) /* use found value */
					retval+=(this_find.info.data16 >> 9) & 1;
				/* and cache next item at the head of the list (if any) */
				if (this_find.next != null) {
					finder = this_find.next;
					this_find.next=finder.next;
					finder.next=list.next;
					list.next=finder;				}			}
			if (info.idx>=0)
				info.idx++;		}
		retval+=found*4-missed;
		if (finder_idx>0)
			list=core_list_mergesort(list,cmp_complex,res);
		remover=core_list_remove(list.next);
		finder=core_list_find(list,info);
		if (finder==null)
			finder=list.next;
		while (finder!=null) {
			retval=CoreUtil.crc16(list.info.data16,retval);
			finder=finder.next;		}
		remover=core_list_undo_remove(remover, list.next);
		list=core_list_mergesort(list,cmp_idx,null);
		finder=list.next;
		while (finder!=null) {
			retval=CoreUtil.crc16(list.info.data16,retval);
			finder=finder.next;		}
		return retval;	}
	static ListHead core_list_init(int blksize, short seed) {
		System.out.println("Using CoreListJoinA");
		int per_item=16+4, size=(blksize/per_item)-2; /* to accomodate systems with 64b pointers, and make sure same code is executed, set max list elements */
 		ShortWrapper memblock = new ShortWrapper();
 		memblock.value = 0;
		short memblock_end=(short)(size*2); // *2 because in the C version we count in pointers to list_head structs, which are 4 bytes, but in Java we count 2 byte shorts. So we need to reserve *2 as much memory.
		ShortWrapper datablock = new ShortWrapper();
		datablock.value = memblock_end;
		short datablock_end=(short)(datablock.value+(size*2)); // *2 because in the C version we count in pointers to list_data structs, which are 4 bytes, but in Java we count 2 byte shorts. So we need to reserve *2 as much memory.
		int i;
		ListHead finder, list=new ListHead();
		ListData info=new ListData();
		list.next=null;
		list.info=new ListData();
		list.info.idx=0x0000;
		list.info.data16=0x8080;
		memblock.value=(short)(memblock.value+2); // +2 because we're counting shorts instead of structs
		datablock.value=(short)(datablock.value+2); // +2 because we're counting shorts instead of structs
		info.idx=0x7fff;
		info.data16=0xffff;
		core_list_insert_new(list,info,memblock,datablock,memblock_end,datablock_end);
		for (i=0; i<size; i++) {
			short datpat=(short)((seed^i) & 0xf);
			short dat=(short)((datpat<<3) | (i&0x7)); /* alternate between algorithms */
			info.data16=(short)((dat<<8) | dat);		/* fill the data with actual data and upper bits with rebuild value */
			core_list_insert_new(list,info,memblock,datablock,memblock_end,datablock_end);		}
		finder=list.next;
		i=1;
		while (finder.next!=null) {
			if (i<size/5) /* first 20% of the list in order */
				finder.info.idx=(short)(i++);
			else { 
				short pat=(short)(i++ ^ seed); /* get a pseudo random number */
				finder.info.idx=(short)(0x3fff & (((i & 0x07) << 8) | pat)); /* make sure the mixed items end up after the ones in sequence */			}
			finder=finder.next;		}
		list = core_list_mergesort(list,cmp_idx,null);
  		return list;	}
	static ListHead core_list_insert_new(ListHead insert_point, ListData info, ShortWrapper memblock, ShortWrapper datablock		, short memblock_end, short datablock_end) {
		ListHead newitem;
		short memblock_val = memblock.value, datablock_val = datablock.value;
		if ((memblock_val+2) >= memblock_end) // +2 because it's not a list_head pointer anymore, so we need to count 2 shorts instead of 1 struct
			return null;
		if ((datablock_val+2) >= datablock_end) // +2 because it's not a list_head pointer anymore, so we need to count 2 shorts instead of 1 struct
			return null;			
		newitem=new ListHead();
		memblock.value=(short)(memblock_val+2); // +2, see above
		newitem.next=insert_point.next;
		insert_point.next=newitem;		
		newitem.info=new ListData();
		datablock.value=(short)(datablock_val+2); // +2, see above
		newitem.info.idx=info.idx;
		newitem.info.data16=info.data16;
		return newitem;	}
	static ListHead core_list_remove(ListHead item) {
		ListData tmp;
		ListHead ret=item.next;
		tmp=item.info;
		item.info=ret.info;
		ret.info=tmp;
		item.next=item.next.next;
		ret.next=null;
		return ret;	}
	static ListHead core_list_undo_remove(ListHead item_removed, ListHead item_modified) {
		ListData tmp;
		tmp=item_removed.info;
		item_removed.info=item_modified.info;
		item_modified.info=tmp;
		item_removed.next=item_modified.next;
		item_modified.next=item_removed;
		return item_removed;	}
	@Lightweight
	static ListHead core_list_find(ListHead list, ListData info) {
		if (info.idx>=0) {
			while (list != null && (list.info.idx != info.idx))
				list=list.next;
			return list;
		} else {
			while (list != null && ((list.info.data16 & 0xff) != info.data16))
				list=list.next;
			return list;		}	}
	static ListHead core_list_reverse(ListHead list) {
		ListHead next=null, tmp;
		while (list!=null) {
			tmp=list.next;
			list.next=next;
			next=list;
			list=tmp;		}
		return next;	}
	static ListHead core_list_mergesort(ListHead list, AbstractListDataCompare cmp, CoreResults res) {
	    ListHead p, q, e, tail;
	    int insize, nmerges, psize, qsize, i;
	    insize = 1;
	    while (true) {
	        p = list;
	        list = null;
	        tail = null;
	        nmerges = 0;  /* count number of merges we do in this pass */
	        while (p!=null) {
	            nmerges++;  /* there exists a merge to be done */
	            /* step `insize' places along from p */
	            q = p;
	            psize = 0;
	            for (i = 0; i < insize; i++) {
	                psize++;
				    q = q.next;
	                if (q==null) break;	            }
	            qsize = insize;
	            while (psize > 0 || (qsize > 0 && q!=null)) {
					if (psize == 0) {
					    /* p is empty; e must come from q. */
					    e = q; q = q.next; qsize--;
					} else if (qsize == 0 || q==null) {
					    /* q is empty; e must come from p. */
					    e = p; p = p.next; psize--;
					} else if (cmp.compare(p.info,q.info,res) <= 0) {
					    /* First element of p is lower (or same); e must come from p. */
					    e = p; p = p.next; psize--;
					} else {
					    /* First element of q is lower; e must come from q. */
					    e = q; q = q.next; qsize--;					}
					if (tail!=null) {
					    tail.next = e;
					} else {
					    list = e;					}
					tail = e;		        }
				p = q;	        }
		    tail.next = null;
	        if (nmerges <= 1)   /* allow for nmerges==0, the empty list case */
	            return list;
	        insize *= 2;	    }	}}
public class CoreMarkH {
	public static short TOTAL_DATA_SIZE		= 2*1000;
	public static byte ID_LIST				= (1<<0);
	public static byte ID_MATRIX 			= (1<<1);
	public static byte ID_STATE 			= (1<<2);
	public static byte ALL_ALGORITHMS_MASK	= (byte)(ID_LIST|ID_MATRIX|ID_STATE);
	public static byte NUM_ALGORITHMS		= 3;}
public class CoreMatrix {
	public static class MatParams {
		public short N;
		public short[] A;
		public short[] B;
		public int[] C;	};
	static short matrix_clip(short x, boolean y) { return (y ? (short)(x & 0x0ff) : (short)(x & 0x0ffff)); }
	static short core_bench_matrix(MatParams p, short seed, short crc) {
		short N=p.N;
		int[] C=p.C;
		short[] A=p.A;
		short[] B=p.B;
		short val=seed;
		crc=CoreUtil.crc16(matrix_test(N,C,A,B,val),crc);
		return crc;	}
	static short matrix_test(short N, int[] C, short[] A, short[] B, short val) {
		short crc=0;
		// short clipval=matrix_big(val);
		short clipval=(short)(0xf000 | val);
		matrix_add_const(N,A,val); /* make sure data changes  */
		matrix_mul_const(N,C,A,val);
		crc=CoreUtil.crc16(matrix_sum(N,C,clipval),crc);
		matrix_mul_vect(N,C,A,B);
		crc=CoreUtil.crc16(matrix_sum(N,C,clipval),crc);
		matrix_mul_matrix(N,C,A,B);
		crc=CoreUtil.crc16(matrix_sum(N,C,clipval),crc);
		matrix_mul_matrix_bitextract(N,C,A,B);
		crc=CoreUtil.crc16(matrix_sum(N,C,clipval),crc);
		matrix_add_const(N,A,(short)-val); /* return matrix to initial value */
		return crc;	}
	static short core_init_matrix(short blksize, int seed, MatParams p) {
		short N=0, order=1, val,i=0,j=0;
		short[] A, B;
		if (seed==0)
			seed=1;
		while (j<blksize) {
			i++;
			j=(short)(i*i*2*4);		}
		N=(short)(i-1);
		A=new short[N*N];
		B=new short[N*N];
		short i_times_N = 0;
		for (i=0; i<N; i++) {
			for (j=0; j<N; j++) {
				seed = ( ( order * seed ) % 65536 );
				val = (short)(seed + order);
				val=matrix_clip(val,false);
				B[i_times_N+j] = val;
				val = (short)(val + order);
				val=matrix_clip(val,true);
				A[i_times_N+j] = val;
				order++;			}
			i_times_N += N;		}
		p.A=A;
		p.B=B;
		p.C=new int[N*N];
		p.N=N;
		return N;	}
	@Lightweight(rank=4) // Needs to come after crc
	static short matrix_sum(short N, int[] C, short clipval) {
		int tmp=0,prev=0,cur=0;
		short ret=0;
		short i,j;
		short i_times_N = 0;
		for (i=0; i<N; i++) {
			short i_times_N_plus_N = (short)(i_times_N + N);
			for (j=i_times_N; j<i_times_N_plus_N; j++) {
				cur=C[j];
				tmp+=cur;
				if (tmp>clipval) {
					ret+=10;
					tmp=0;
				} else {
					if (cur>prev) {
						ret++;					}				}
				prev=cur;			}
			i_times_N += N;		}
		return ret;	}
	static void matrix_mul_const(short N, int[] C, short[] A, short val) {
		short i,j;
		short i_times_N = 0;
		for (i=0; i<N; i++) {
			short i_times_N_plus_N = (short)(i_times_N + N);
			for (j=i_times_N; j<i_times_N_plus_N; j++) {
				C[j]=A[j] * val;			}
			i_times_N += N;		}	}
	static void matrix_add_const(short N, short[] A, short val) {
		short i,j;
		short i_times_N = 0;
		for (i=0; i<N; i++) {
			short i_times_N_plus_N = (short)(i_times_N + N);
			for (j=i_times_N; j<i_times_N_plus_N; j++) {
				A[j] += val;			}
							i_times_N += N;		}	}
	static void matrix_mul_vect(short N, int[] C, short[] A, short[] B) {
		short i,j;
		short i_times_N = 0;
		for (i=0; i<N; i++) {
			int C_value = 0;
			for (j=0; j<N; j++) {
				C_value+=A[i_times_N+j] * B[j];			}
			C[i]=C_value;
			i_times_N += N;		}	}
	static void matrix_mul_matrix(short N, int[] C, short[] A, short[] B) {
		short i,j,k;
		short i_times_N = 0;
		for (i=0; i<N; i++) {
			for (j=0; j<N; j++) {
				int C_value = 0;
				for(k=0;k<N;k++)				{
					C_value += A[i_times_N+k] * B[k*N+j];				}
				C[i_times_N+j]=C_value;			}
			i_times_N += N;		}	}
	static void matrix_mul_matrix_bitextract(short N, int[] C, short[] A, short[] B) {
		short i,j,k, i_times_N = 0;
		for (i=0; i<N; i++) {
			for (j=0; j<N; j++) {
				int C_value = 0;
				for(k=0;k<N;k++)				{
					int tmp=A[i_times_N+k] * B[k*N+j];
					C_value += (short)((tmp>>2) & (~(0xffffffff << 4))) * (short)((tmp>>5) & (~(0xffffffff << 7)));				}
				C[i_times_N+j] = C_value;			}
			i_times_N += N;		}	}}
public class CorePortMe {
	public static boolean PROFILE_RUN, PERFORMANCE_RUN, VALIDATION_RUN;
	public static final int ITERATIONS                      = 50;
	static {
		if (!PROFILE_RUN && !PERFORMANCE_RUN && !VALIDATION_RUN) {
			if (CoreMarkH.TOTAL_DATA_SIZE==1200) {
				PROFILE_RUN=true;
			} else if (CoreMarkH.TOTAL_DATA_SIZE==2000) {
				PERFORMANCE_RUN=true;
			} else {
				VALIDATION_RUN=true;			}		}
		if (VALIDATION_RUN) {
			CoreUtil.seed1_volatile=0x3415;
			CoreUtil.seed2_volatile=0x3415;
			CoreUtil.seed3_volatile=0x66;		}
		if (PERFORMANCE_RUN) {
			CoreUtil.seed1_volatile=0x0;
			CoreUtil.seed2_volatile=0x0;
			CoreUtil.seed3_volatile=0x66;		}
		if (PROFILE_RUN) {
			CoreUtil.seed1_volatile=0x8;
			CoreUtil.seed2_volatile=0x8;
			CoreUtil.seed3_volatile=0x8;		}
		CoreUtil.seed4_volatile=ITERATIONS;
		CoreUtil.seed5_volatile=0;			}
	public static final int EE_TICKS_PER_SEC				= 1000;
	public static int default_num_contexts					= 1;
	public static int time_in_secs(int ticks) {
		// int retval = ticks / EE_TICKS_PER_SEC;
		int retval = ticks / 1000;
		return retval;	}
	public static int time_in_msecs(int ticks) {
		return ticks;	}}
public class CoreResults {
	public short	seed1, seed2, seed3;
	public int size, iterations,  execs;		/* Bitmask of operations to execute */
	CoreMatrix.MatParams mat;
	byte[] statememblock3;
	CoreListJoinA.ListHead list_CoreListJoinA; // for the A implementation, the pointer to the list head.
	short list_CoreListJoinB; // for the B implementation, the pointer to the list is simply the index into the static data array in CoreListJoinB that will be returned by core_list_init.
	short	crc,	crclist,	crcmatrix	, crcstate ;
	public short	err;
}
public class CoreState {
	public static final byte CORE_STATE_START = 0;
	public static final byte CORE_STATE_INVALID = 1;
	public static final byte CORE_STATE_S1 = 2;
	public static final byte CORE_STATE_S2 = 3;
	public static final byte CORE_STATE_INT = 4;
	public static final byte CORE_STATE_FLOAT = 5;
	public static final byte CORE_STATE_EXPONENT = 6;
	public static final byte CORE_STATE_SCIENTIFIC = 7;
	public static final byte NUM_CORE_STATES = 8;
	static short core_bench_state(int blksize, byte[] memblock, 
			short seed1, short seed2, short step, short crc) 	{
		int[] final_counts = new int[NUM_CORE_STATES];
		int[] track_counts = new int[NUM_CORE_STATES];
		ShortWrapper p = new ShortWrapper();
		short pValue; // Within this method we use this so the local variable can be pinned by markloop.
		p.value=0;    // We use this to pass to core_state_transition since it needs to be able to modify the index (it's a double pointer in C).
		int i;
		for (i=0; i<NUM_CORE_STATES; i++) {
			final_counts[i]=track_counts[i]=0;		}
		while (memblock[p.value]!=0) {
			byte fstate=core_state_transition(p,memblock,track_counts);
			final_counts[fstate]++;		}
		pValue=0; // Stays within this method, so use pValue
		while (pValue < blksize) { /* insert some corruption */
			if (memblock[pValue]!=',')
				memblock[pValue]^=(byte)seed1;
			pValue+=step;		}
		p.value=0;
		while (memblock[p.value]!=0) {
			byte fstate=core_state_transition(p,memblock,track_counts);
			final_counts[fstate]++;		}
		pValue=0; // Stays within this method, so use pValue
		while (pValue < blksize) { /* undo corruption if seed1 and seed2 are equal */
			if (memblock[pValue]!=',')
				memblock[pValue]^=(byte)seed2;
			pValue+=step;		}
		for (i=0; i<NUM_CORE_STATES; i++) {
			crc=CoreUtil.crcu32(final_counts[i],crc);
			crc=CoreUtil.crcu32(track_counts[i],crc);		}
		return crc;	}
	private static byte[] intpat(int i) {
			 if (i==0) return     "5012".getBytes();
		else if (i==1) return     "1234".getBytes();
		else if (i==2) return     "-874".getBytes();
		else           return     "+122".getBytes();	}
	private static byte[] floatpat(int i) {
			 if (i==0) return "35.54400".getBytes();
		else if (i==1) return ".1234500".getBytes();
		else if (i==2) return "-110.700".getBytes();
		else           return "+0.64400".getBytes();	}
	private static byte[] scipat(int i) {
			 if (i==0) return "5.500e+3".getBytes();
		else if (i==1) return "-.123e-2".getBytes();
		else if (i==2) return "-87e+832".getBytes();
		else           return "+0.6e-12".getBytes();	}
	private static byte[] errpat(int i) {
			 if (i==0) return "T0.3e-1F".getBytes();
		else if (i==1) return "-T.T++Tq".getBytes();
		else if (i==2) return "1T3.4e4z".getBytes();
		else           return "34.0e-T^".getBytes();	}
	static byte[] core_init_state(int size, short seed) {
		byte[] p=new byte[size];
		int total=0,next=0,i;
		byte[] buf=null;
		size--;
		next=0;
		while ((total+next+1)<size) {
			if (next>0) {
				for(i=0;i<next;i++)
					p[total+i]=buf[i];
				p[total+i]=',';
				total+=next+1;			}
			seed++;
			switch (seed & 0x7) {
				case 0: /* int */
				case 1: /* int */
				case 2: /* int */
					buf=intpat((seed>>3) & 0x3);
					next=4;
				break;
				case 3: /* float */
				case 4: /* float */
					buf=floatpat((seed>>3) & 0x3);
					next=8;
				break;
				case 5: /* scientific */
				case 6: /* scientific */
					buf=scipat((seed>>3) & 0x3);
					next=8;
				break;
				case 7: /* invalid */
					buf=errpat((seed>>3) & 0x3);
					next=8;
				break;
				default: /* Never happen, just to make some compilers happy */
				break;			}		}
		size++;
		while (total<size) { /* fill the rest with 0 */
			p[total]=0;
			total++;		}
		return p;	}
	private static class CoreStateTransitionParam {
		public byte[] instr;
		public short index;	}
	@Lightweight
	static byte core_state_transition(ShortWrapper indexWrapper, byte[] str, int[] transition_count) {
		short index = indexWrapper.value;
		byte NEXT_SYMBOL;
		byte state=CORE_STATE_START;
		for( ; (NEXT_SYMBOL = str[index])!=0 && state != CORE_STATE_INVALID; index++ ) {
			if (NEXT_SYMBOL==',') /* end of this input */ {
				index++;
				break;			}
			switch(state) {
				case CORE_STATE_START:
					if((NEXT_SYMBOL>='0') && (NEXT_SYMBOL<='9')) {
						state = CORE_STATE_INT;					}
					else if( NEXT_SYMBOL == '+' || NEXT_SYMBOL == '-' ) {
						state = CORE_STATE_S1;					}
					else if( NEXT_SYMBOL == '.' ) {
						state = CORE_STATE_FLOAT;					}
					else {
						state = CORE_STATE_INVALID;
						transition_count[CORE_STATE_INVALID]++;					}
					transition_count[CORE_STATE_START]++;
					break;
				case CORE_STATE_S1:
					if((NEXT_SYMBOL>='0') && (NEXT_SYMBOL<='9')) {
						state = CORE_STATE_INT;
						transition_count[CORE_STATE_S1]++;					}
					else if( NEXT_SYMBOL == '.' ) {
						state = CORE_STATE_FLOAT;
						transition_count[CORE_STATE_S1]++;					}
					else {
						state = CORE_STATE_INVALID;
						transition_count[CORE_STATE_S1]++;					}
					break;
				case CORE_STATE_INT:
					if( NEXT_SYMBOL == '.' ) {
						state = CORE_STATE_FLOAT;
						transition_count[CORE_STATE_INT]++;					}
					else if(!((NEXT_SYMBOL>='0') && (NEXT_SYMBOL<='9'))) {
						state = CORE_STATE_INVALID;
						transition_count[CORE_STATE_INT]++;					}
					break;
				case CORE_STATE_FLOAT:
					if( NEXT_SYMBOL == 'E' || NEXT_SYMBOL == 'e' ) {
						state = CORE_STATE_S2;
						transition_count[CORE_STATE_FLOAT]++;					}
					else if(!((NEXT_SYMBOL>='0') && (NEXT_SYMBOL<='9'))) {
						state = CORE_STATE_INVALID;
						transition_count[CORE_STATE_FLOAT]++;					}
					break;
				case CORE_STATE_S2:
					if( NEXT_SYMBOL == '+' || NEXT_SYMBOL == '-' ) {
						state = CORE_STATE_EXPONENT;
						transition_count[CORE_STATE_S2]++;					}
					else {
						state = CORE_STATE_INVALID;
						transition_count[CORE_STATE_S2]++;					}
					break;
				case CORE_STATE_EXPONENT:
					if((NEXT_SYMBOL>='0') && (NEXT_SYMBOL<='9')) {
						state = CORE_STATE_SCIENTIFIC;
						transition_count[CORE_STATE_EXPONENT]++;					}
					else {
						state = CORE_STATE_INVALID;
						transition_count[CORE_STATE_EXPONENT]++;					}
					break;
				case CORE_STATE_SCIENTIFIC:
					if(!((NEXT_SYMBOL>='0') && (NEXT_SYMBOL<='9'))) {
						state = CORE_STATE_INVALID;
						transition_count[CORE_STATE_INVALID]++;					}
					break;
				default:
					break;			}		}
		indexWrapper.value = index;
		return state;	}}
public class CoreUtil {
	public static int seed1_volatile;
	public static int seed2_volatile;
	public static int seed3_volatile;
	public static int seed4_volatile;
	public static int seed5_volatile;
	public static int get_seed_32(int i) {
		int retval;
		switch (i) {
			case 1:
				retval=seed1_volatile;
				break;
			case 2:
				retval=seed2_volatile;
				break;
			case 3:
				retval=seed3_volatile;
				break;
			case 4:
				retval=seed4_volatile;
				break;
			case 5:
				retval=seed5_volatile;
				break;
			default:
				retval=0;
				break;		}
		return retval;	}
	@Lightweight(rank=2)
	static short crcu16(short newval, short crc) {
		byte data = (byte)newval;
		for (short i = -8; i != 0; i++) // This is faster because a !=0 check is faster than <8.	    {
			if (((data ^ crc) & 1) == 0)			{
				crc >>>= 1;
				crc &= 0x7fff;			}
			else			{
				crc ^= 0x4002;
				crc >>>= 1;
				crc |= -0x8000;			}
			data >>>= 1;	    }
		data = (byte)(newval>>>8);
		for (short i = -8; i != 0; i++) // This is faster because a !=0 check is faster than <8.	    {
			if (((data ^ crc) & 1) == 0)			{
				crc >>>= 1;
				crc &= 0x7fff;			}
			else			{
				crc ^= 0x4002;
				crc >>>= 1;
				crc |= -0x8000;			}
			data >>>= 1;	    }
		return crc;	}
	@Lightweight(rank=4)
	static short crcu32(int newval, short crc) {
		crc=crc16((short) newval, crc);
		crc=crc16((short) (newval>>>16), crc);
		return crc;	}
	@Lightweight(rank=3)
	static short crc16(short newval, short crc) {
		return crcu16((short)newval, crc);	}
public class ShortWrapper {
	public short value;}