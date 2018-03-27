public class CoreListJoinA {
	public static class ListData {
		public short data16;
		public short idx;	}
	public static class ListHead {
		ListHead next;
		ListData info;	}
		@Lightweight(rank=4) 
		static short calc_func(ListData pdata, CoreResults res, CoreMain.TmpData tmpData) {
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
						retval=CoreState.core_bench_state(res.size,res.statememblock3,res.seed1,res.seed2,dtype,res.crc, tmpData);
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
		public static int cmp_complex(ListData a, ListData b, CoreResults res, CoreMain.TmpData tmpData) {
			short val1=calc_func(a,res,tmpData);
			short val2=calc_func(b,res,tmpData);
			return val1 - val2;		}
		public static int cmp_idx(ListData a, ListData b, CoreResults res, CoreMain.TmpData tmpData) {
			if (res==null) {
				a.data16=(short)((a.data16 & 0xff00) | (0x00ff & (a.data16>>8)));
				b.data16=(short)((b.data16 & 0xff00) | (0x00ff & (b.data16>>8)));			}
			return a.idx - b.idx;		}
	static short core_bench_list(CoreResults res, short finder_idx, CoreMain.TmpData tmpData) {
		short retval=0;
		short found=0,missed=0;
		ListHead list=res.list_CoreListJoinA;
		short find_num=res.seed3;
		ListHead this_find;
		ListHead finder, remover;
		ListData info=tmpData.info;
		short i;
		info.idx=finder_idx;
		for (i=0; i<find_num; i++) {
			info.data16=(short)(i & 0xff);
			this_find=core_list_find(list,info);
			list=core_list_reverse(list);
			if (this_find==null) {
				missed++;
				retval+=(list.next.info.data16 >> 8) & 1;
			} else {
				found++;
				if ((this_find.info.data16 & 0x1) != 0) /* use found value */
					retval+=(this_find.info.data16 >> 9) & 1;
				if (this_find.next != null) {
					finder = this_find.next;
					this_find.next=finder.next;
					finder.next=list.next;
					list.next=finder;				}			}
			if (info.idx>=0)
				info.idx++;		}
		retval+=found*4-missed;
		if (finder_idx>0)
			list=core_list_mergesort(list,false,res,tmpData);
		remover=core_list_remove(list.next);
		finder=core_list_find(list,info);
		if (finder==null)
			finder=list.next;
		while (finder!=null) {
			retval=CoreUtil.crc16(list.info.data16,retval);
			finder=finder.next;		}
		remover=core_list_undo_remove(remover, list.next);
		list=core_list_mergesort(list,true,null,tmpData);
		finder=list.next;
		while (finder!=null) {
			retval=CoreUtil.crc16(list.info.data16,retval);
			finder=finder.next;		}
		return retval;	}
	static ListHead core_list_init(int blksize, short seed, CoreMain.TmpData tmpData) {
		System.out.println("Using CoreListJoinA");
		ListData info = tmpData.info;
		int per_item=16+4; 
		int size=(blksize/per_item)-2; /* to accomodate systems with 64b pointers, and make sure same code is executed, set max list elements */ 
 		ShortWrapper memblock = new ShortWrapper();
 		memblock.value = 0;
		short memblock_end=(short)(size*2); 
		ShortWrapper datablock = new ShortWrapper();
		datablock.value = memblock_end;
		short datablock_end=(short)(datablock.value+(size*2)); 
		int i;
		ListHead finder, list=new ListHead();
		list.next=null;
		list.info=new ListData();
		list.info.idx=0x0000;
		list.info.data16=0x8080;
		memblock.value=(short)(memblock.value+2); 
		datablock.value=(short)(datablock.value+2); 
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
				finder.info.idx=(i++);
			else { 
				short pat=(short)(i++ ^ seed); /* get a pseudo random number */
				finder.info.idx=(short)(0x3fff & (((i & 0x07) << 8) | pat)); /* make sure the mixed items end up after the ones in sequence */			}
			finder=finder.next;		}
		list = core_list_mergesort(list,true,null,tmpData);
 		return list;	}
	static ListHead core_list_insert_new(ListHead insert_point, ListData info, ShortWrapper memblock, ShortWrapper datablock, short memblock_end, short datablock_end) {
		ListHead newitem;
		short memblock_val = memblock.value;
		short datablock_val = datablock.value;
		if ((memblock_val+2) >= memblock_end) 
			return null;
		if ((datablock_val+2) >= datablock_end) 
			return null;
		newitem=new ListHead();
		memblock.value=(short)(memblock_val+2); 
		newitem.next=insert_point.next;
		insert_point.next=newitem;
		newitem.info=new ListData();
		datablock.value=(short)(datablock_val+2); 
		newitem.info.idx=info.idx;
		newitem.info.data16=info.data16;
		return newitem;	}
	static ListHead core_list_remove(ListHead item) {
		ListData tmp;
		ListHead ret=item.next;
		/* swap data pointers */
		tmp=item.info;
		item.info=ret.info;
		ret.info=tmp;
		/* and eliminate item */
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
	static ListHead core_list_mergesort(ListHead list, boolean useIdxCompare, CoreResults res, CoreMain.TmpData tmpData) {
	    ListHead p, q, e, tail;
	    int insize, nmerges, psize, qsize, i;
	    insize = 1;
	    while (true) {
	        p = list;
	        list = null;
	        tail = null;
	        nmerges = 0;  
	        while (p!=null) {
	            nmerges++;  
	            q = p;
	            psize = 0;
	            for (i = 0; i < insize; i++) {
	                psize++;
				    q = q.next;
	                if (q==null) break;
	            }
	            qsize = insize;
	            while (psize > 0 || (qsize > 0 && q!=null)) {
					if (psize == 0) {					    
					    e = q; q = q.next; qsize--;
					} else if (qsize == 0 || q==null) {
					    e = p; p = p.next; psize--;
					} else {
						int rv;
						if (useIdxCompare) {
							rv = cmp_idx(p.info,q.info,res,tmpData);
						} else {
							rv = cmp_complex(p.info,q.info,res,tmpData);						}
						if (rv <= 0) {
						    e = p; p = p.next; psize--;
						} else {						    
						    e = q; q = q.next; qsize--;						}					}
					if (tail!=null) {
					    tail.next = e;
					} else {
					    list = e;					}
					tail = e;		        }
				p = q;	        }			
		    tail.next = null;
	        if (nmerges <= 1)   
	            return list;
	        insize *= 2;	    }	}}
public class CoreMain {
	public static class TmpData {
		public CoreListJoinA.ListData info;
		public int[] final_counts;
		public int[] track_counts;
		public ShortWrapper p;
		public TmpData(short arraySize) {
			this.info = new CoreListJoinA.ListData();
			this.final_counts = new int[arraySize];
			this.track_counts = new int[arraySize];
			this.p = new ShortWrapper();		}			}
	private static final boolean useCoreListJoinA = true;
	@ConstArray
	private static class list_known_crc {
		public static short[] data = {(short)0xd4b0,(short)0x3340,(short)0x6a79,(short)0xe714,(short)0xe3c1};	}
	@ConstArray
	private static class matrix_known_crc {
		public static short[] data = {(short)0xbe52,(short)0x1199,(short)0x5608,(short)0x1fd7,(short)0x0747};	}
	@ConstArray
	private static class state_known_crc {
		public static short[] data = {(short)0x5e47,(short)0x39bf,(short)0xe5a4,(short)0x8e3a,(short)0x8d84};	}
	private static void iterate(CoreResults pres, TmpData tmpData) {
		int i;
		short crc;
		CoreResults res=pres;
		int iterations=res.iterations;
		res.crc=0;
		res.crclist=0;
		res.crcmatrix=0;
		res.crcstate=0;
		if (useCoreListJoinA) {
			for (i=0; i<iterations; i++) {
				crc=CoreListJoinA.core_bench_list(res,(short)1, tmpData);
				res.crc=CoreUtil.crcu16(crc,res.crc);
				crc=CoreListJoinA.core_bench_list(res,(short)-1, tmpData);
				res.crc=CoreUtil.crcu16(crc,res.crc);
				if (i==0) res.crclist=res.crc;}
		} else {
			for (i=0; i<iterations; i++) {
				crc=CoreListJoinB.core_bench_list(res,(short)1, tmpData);
				res.crc=CoreUtil.crcu16(crc,res.crc);
				crc=CoreListJoinB.core_bench_list(res,(short)-1, tmpData);
				res.crc=CoreUtil.crcu16(crc,res.crc);
				if (i==0) res.crclist=res.crc;			}		}
		return;	}
	private static short MULTITHREAD=1; 
	private static int get_seed_32(int i) {
		return CoreUtil.get_seed_32(i);	}
	private static short get_seed(int x) {
		return (short)CoreUtil.get_seed_32(x);	}
	public static void rtcbenchmark_measure_java_performance(CoreResults pres, CoreMain.TmpData tmpData) {
		iterate(pres, tmpData);	}
	public static boolean core_mark_main() {
		short i,j=0,num_algorithms=0;
		short known_id=-1,total_errors=0;
		short seedcrc=0;
		int total_time;
		CoreResults[] results = new CoreResults[MULTITHREAD];
		TmpData tmpData = new TmpData(CoreState.NUM_CORE_STATES);
		for (i=0 ; i<MULTITHREAD; i++) {
			results[i] = new CoreResults();		}
		results[0].seed1=get_seed(1);
		results[0].seed2=get_seed(2);
		results[0].seed3=get_seed(3);
		results[0].iterations=get_seed_32(4);
		results[0].execs=get_seed_32(5);
		if (results[0].execs==0) { /* if not supplied, execute all algorithms */
			results[0].execs=CoreMarkH.ALL_ALGORITHMS_MASK;		}
		/* put in some default values based on one seed only for easy testing */
		if ((results[0].seed1==0) && (results[0].seed2==0) && (results[0].seed3==0)) { /* validation run */
			results[0].seed1=0;
			results[0].seed2=0;
			results[0].seed3=0x66;		}
		if ((results[0].seed1==1) && (results[0].seed2==0) && (results[0].seed3==0)) { /* perfromance run */
			results[0].seed1=0x3415;
			results[0].seed2=0x3415;
			results[0].seed3=0x66;		}
		results[0].size=CoreMarkH.TOTAL_DATA_SIZE;
		results[0].err=0;
		for (i=0; i<CoreMarkH.NUM_ALGORITHMS; i++) {
			if (((1<<i) & results[0].execs) != 0)
				num_algorithms++;		}
		for (i=0 ; i<MULTITHREAD; i++) 
			results[i].size=results[i].size/num_algorithms;
		for (i=0 ; i<MULTITHREAD; i++) {
			if ((results[i].execs & CoreMarkH.ID_LIST) != 0) {
				if (useCoreListJoinA) {
					results[i].list_CoreListJoinA=CoreListJoinA.core_list_init(results[0].size,results[i].seed1,tmpData);
				} else {
					results[i].list_CoreListJoinB=CoreListJoinB.core_list_init(results[0].size,results[i].seed1,tmpData);				}			}
			if ((results[i].execs & CoreMarkH.ID_MATRIX) != 0) {
				results[i].mat = new CoreMatrix.MatParams();
				CoreMatrix.core_init_matrix((short)results[0].size, results[i].seed1 | ((results[i].seed2) << 16), results[i].mat );			}
			if ((results[i].execs & CoreMarkH.ID_STATE) != 0) {
				results[i].statememblock3 = CoreState.core_init_state(results[0].size,results[i].seed1);			}		}
		if (results[0].iterations==0) { 
			int secs_passed=0;
			int divisor;
			results[0].iterations=1;
			while (secs_passed < 1) {
				results[0].iterations*=10;
				RTC.coremark_start_time();
				iterate(results[0], tmpData);
				RTC.coremark_stop_time();
				secs_passed=CorePortMe.time_in_secs(RTC.coremark_get_time());			}
			divisor=secs_passed;
			if (divisor==0) /* some machines cast float to int as 0 since this conversion is not defined by ANSI, but we know at least one second passed */
				divisor=1;
			results[0].iterations*=1+10/divisor;		}		
		RTC.avroraStartCountingCalls();
		RTC.coremark_start_time();
		rtcbenchmark_measure_java_performance(results[0], tmpData);
		RTC.coremark_stop_time();
		total_time=RTC.coremark_get_time();
		RTC.avroraStopCountingCalls();
		results[0].mat = null;
		results[0].statememblock3 = null;
		results[0].list_CoreListJoinA = null;
		CoreListJoinB.data = null;
		tmpData = null;
		seedcrc=CoreUtil.crc16(results[0].seed1,seedcrc);
		seedcrc=CoreUtil.crc16(results[0].seed2,seedcrc);
		seedcrc=CoreUtil.crc16(results[0].seed3,seedcrc);
		seedcrc=CoreUtil.crc16((short)results[0].size,seedcrc);		
		switch (seedcrc) { /* test known output for common seeds */
			case 0x8a02: /* seed1=0, seed2=0, seed3=0x66, size 2000 per algorithm */
				known_id=0;
				System.out.println("6k performance run parameters for coremark.");
				break;
			case 0x7b05: /*  seed1=0x3415, seed2=0x3415, seed3=0x66, size 2000 per algorithm */
				known_id=1;
				System.out.println("6k validation run parameters for coremark.");
				break;
			case 0x4eaf: /* seed1=0x8, seed2=0x8, seed3=0x8, size 400 per algorithm */
				known_id=2;
				System.out.println("Profile generation run parameters for coremark.");
				break;
			case 0xe9f5: /* seed1=0, seed2=0, seed3=0x66, size 666 per algorithm */
				known_id=3;
				System.out.println("2K performance run parameters for coremark.");
				break;
			case 0x18f2: /*  seed1=0x3415, seed2=0x3415, seed3=0x66, size 666 per algorithm */
				known_id=4;
				System.out.println("2K validation run parameters for coremark.");
				break;
			default:
				total_errors=-1;
				break;		}
		if (known_id>=0) {
			for (i=0 ; i<CorePortMe.default_num_contexts; i++) {
				results[i].err=0;

				if ((results[i].execs & CoreMarkH.ID_LIST) != 0 && 
					(results[i].crclist!=list_known_crc.data[known_id])) {
					System.out.println("[" + i + "]ERROR! list crc 0x" + Integer.toHexString(results[i].crclist) + " - should be 0x" + Integer.toHexString(list_known_crc.data[known_id]));
					results[i].err++;				}
				if ((results[i].execs & CoreMarkH.ID_MATRIX) != 0 &&
					(results[i].crcmatrix!=matrix_known_crc.data[known_id])) {
					System.out.println("[" + i + "]ERROR! matrix crc 0x" + Integer.toHexString(results[i].crcmatrix) + " - should be 0x" + Integer.toHexString(matrix_known_crc.data[known_id]));
					results[i].err++;				}
				if ((results[i].execs & CoreMarkH.ID_STATE) != 0 &&
					(results[i].crcstate!=state_known_crc.data[known_id])) {
					System.out.println("[" + i + "]ERROR! state crc 0x" + Integer.toHexString(results[i].crcstate) + " - should be 0x" + Integer.toHexString(state_known_crc.data[known_id]));
					results[i].err++;				}
				total_errors+=results[i].err;			}
		} else {
			System.out.println("ERROR! known_id=" + known_id);
			total_errors+=1;		}
		System.out.println("CoreMark Size    : " + results[0].size);
		System.out.println("Total ticks      : " + total_time);
		System.out.println("Total time (secs): " + CorePortMe.time_in_secs(total_time));
		if (CorePortMe.time_in_secs(total_time) > 0)
			System.out.println("Iterations/Sec   : " + CorePortMe.default_num_contexts*results[0].iterations*1000/CorePortMe.time_in_msecs(total_time));
		if (CorePortMe.time_in_secs(total_time) < 10 && false) { 
			System.out.println("ERROR! Must execute for at least 10 secs for a valid result!");
			total_errors++;		}
		System.out.println("Iterations       : " + CorePortMe.default_num_contexts*results[0].iterations);
		System.out.println("seedcrc          : " + Integer.toHexString(seedcrc & 0xFFFF)); 
		if ((results[0].execs & CoreMarkH.ID_LIST) != 0) {
			for (i=0 ; i<CorePortMe.default_num_contexts; i++) {
				System.out.println("[" + i + "]crclist       : 0x" + Integer.toHexString(results[i].crclist & 0xFFFF)); 			}		}
		if ((results[0].execs & CoreMarkH.ID_MATRIX)  != 0) {
			for (i=0 ; i<CorePortMe.default_num_contexts; i++) {
				System.out.println("[" + i + "]crcmatrix     : 0x" + Integer.toHexString(results[i].crcmatrix & 0xFFFF)); 			}		}
		if ((results[0].execs & CoreMarkH.ID_STATE) != 0) {
			for (i=0 ; i<CorePortMe.default_num_contexts; i++) {
				System.out.println("[" + i + "]crcstate      : 0x" + Integer.toHexString(results[i].crcstate & 0xFFFF)); 			}		}
		for (i=0 ; i<CorePortMe.default_num_contexts; i++) {
			System.out.println("[" + i + "]crcfinal      : 0x" + Integer.toHexString(results[i].crc & 0xFFFF)); 		}
		if (total_errors==0) {
			System.out.println("Correct operation validated. See readme.txt for run and reporting rules.");		}
		if (total_errors>0){
			System.out.println("Errors detected");		}
		if (total_errors<0) {
			System.out.println("Cannot validate operation for these seed values, please compare with results on a known platform.");		}
		return total_errors==0;	}}
public class CoreMarkH {
	public static short TOTAL_DATA_SIZE		= 2*1000;
	public static byte ID_LIST				= (1<<0);
	public static byte ID_MATRIX 			= (1<<1);
	public static byte ID_STATE 			= (1<<2);
	public static byte ALL_ALGORITHMS_MASK	= (byte)(ID_LIST|ID_MATRIX|ID_STATE);
	public static byte NUM_ALGORITHMS		= 3;
}
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
		short N=0;
		short[] A;
		short[] B;
		short order=1;
		short val;
		short i=0,j=0;
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
	@Lightweight(rank=4) 
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
						ret++;					}									}
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
		short i,j,k;
		short i_times_N = 0;
		for (i=0; i<N; i++) {
			for (j=0; j<N; j++) {
				int C_value = 0;
				for(k=0;k<N;k++)				{
					int tmp=A[i_times_N+k] * B[k*N+j];
					C_value += (short)((tmp>>2) & (~(0xffffffff << 4))) * (short)((tmp>>5) & (~(0xffffffff << 7)));				}
				C[i_times_N+j] = C_value;			}
			i_times_N += N;		}	}}
public class CorePortMe {
	public static boolean PROFILE_RUN;
	public static boolean PERFORMANCE_RUN;
	public static boolean VALIDATION_RUN;
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
		int retval = ticks / 1000;
		return retval;	}
	public static int time_in_msecs(int ticks) {
		return ticks;	}}
public class CoreResults {
	public short	seed1;		/* Initializing seed */
	public short	seed2;		/* Initializing seed */
	public short	seed3;		/* Initializing seed */
	public int size;		/* Size of the data */
	public int iterations;		/* Number of iterations to execute */
	public int execs;		/* Bitmask of operations to execute */
	CoreMatrix.MatParams mat;
	byte[] statememblock3;	
	CoreListJoinA.ListHead list_CoreListJoinA; 
	short list_CoreListJoinB; 
	short	crc;
	short	crclist;
	short	crcmatrix;
	short	crcstate;
	public short	err;}
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
	static short core_bench_state(int blksize, byte[] memblock, short seed1, short seed2, short step, short crc, CoreMain.TmpData tmpData) 	{
		int[] final_counts = tmpData.final_counts;
		int[] track_counts = tmpData.track_counts;
		ShortWrapper p = tmpData.p;
		short pValue; 
		p.value=0;    
		int i;
		for (i=0; i<NUM_CORE_STATES; i++) {
			final_counts[i]=track_counts[i]=0;		}
		while (memblock[p.value]!=0) {
			byte fstate=core_state_transition(p,memblock,track_counts);
			final_counts[fstate]++;		}		
		pValue=0; 
		while (pValue < blksize) { /* insert some corruption */
			if (memblock[pValue]!=',')
				memblock[pValue]^=(byte)seed1;
			pValue+=step;		}
		p.value=0;
		while (memblock[p.value]!=0) {
			byte fstate=core_state_transition(p,memblock,track_counts);
			final_counts[fstate]++;		}		
		pValue=0; 
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
	private static byte vliegtuig = 1;
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
		for (short i = (short)-8; i != 0; i++) 	    {
			if (((data ^ crc) & 1) == 0)			{
				crc >>>= 1;
				crc &= (short)0x7fff;			}
			else			{
				crc ^= (short)0x4002;
				crc >>>= 1;
				crc |= (short)-0x8000;			}
			data >>>= 1;	    }
		data = (byte)(newval>>>8);
		for (short i = (short)-8; i != 0; i++) 	    {
			if (((data ^ crc) & 1) == 0)			{
				crc >>>= 1;
				crc &= (short)0x7fff;			}
			else			{
				crc ^= (short)0x4002;
				crc >>>= 1;
				crc |= (short)-0x8000;			}
			data >>>= 1;	    }
		return crc;	}
	@Lightweight(rank=4)
	static short crcu32(int newval, short crc) {
		crc=crc16((short) newval, crc);
		crc=crc16((short) (newval>>>16), crc);
		return crc;	}
	@Lightweight(rank=3)
	static short crc16(short newval, short crc) {
		return crcu16((short)newval, crc);	}}
public class ShortWrapper {
	public short value;}