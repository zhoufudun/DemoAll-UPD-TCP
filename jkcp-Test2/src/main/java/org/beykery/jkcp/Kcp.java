/**
 *  KCP - A Better ARQ Protocol Implementation
 *  skywind3000 (at) gmail.com, 2010-2011
 *  Features:
 *  + Average RTT reduce 30% - 40% vs traditional ARQ like tcp.
 *  + Maximum RTT reduce three times vs tcp.
 *  + Lightweight, distributed as a single source file.
 */
package org.beykery.jkcp;

import java.util.LinkedList;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 *
 * @author beykery
 */
public class Kcp {

	public static final int IKCP_RTO_NDL = 30; // no delay min rto
	public static final int IKCP_RTO_MIN = 100; // normal min rto
	public static final int IKCP_RTO_DEF = 200;
	public static final int IKCP_RTO_MAX = 60000;
	public static final int IKCP_CMD_PUSH = 81; // cmd: push data
	public static final int IKCP_CMD_ACK = 82; // cmd: ack
	public static final int IKCP_CMD_WASK = 83; // cmd: window probe (ask)
	public static final int IKCP_CMD_WINS = 84; // cmd: window size (tell)
	public static final int IKCP_ASK_SEND = 1; // need to send IKCP_CMD_WASK
	public static final int IKCP_ASK_TELL = 2; // need to send IKCP_CMD_WINS
	public static final int IKCP_WND_SND = 32;
	public static final int IKCP_WND_RCV = 32;
	public static final int IKCP_MTU_DEF = 1400;
	public static final int IKCP_ACK_FAST = 3;
	public static final int IKCP_INTERVAL = 100;
	public static final int IKCP_OVERHEAD = 24;
	public static final int IKCP_DEADLINK = 10;
	public static final int IKCP_THRESH_INIT = 2;
	public static final int IKCP_THRESH_MIN = 2;
	public static final int IKCP_PROBE_INIT = 7000; // 7 secs to probe window size
	public static final int IKCP_PROBE_LIMIT = 120000; // up to 120 secs to probe window

	private int conv;
	private int mtu;
	private int mss;
	private int state;
	private int snd_una;// 下一个确认的KCP包序列
	private int snd_nxt;// 下一个发送的kcp包序列号
	private int rcv_nxt;// 下一个接受的kcp包序列号
	private int ts_recent;
	private int ts_lastack;
	private int ssthresh;
	private int rx_rttval;
	private int rx_srtt;
	private int rx_rto;
	private int rx_minrto;
	private int snd_wnd;
	private int rcv_wnd;
	private int rmt_wnd;
	private int cwnd;
	private int probe;
	private int current;
	private int interval;
	private int ts_flush;
	private int xmit;
	private int nodelay;
	private int updated;
	private int ts_probe;
	private int probe_wait;
	private final int dead_link;
	private int incr;
	private final LinkedList<Segment> snd_queue = new LinkedList<>();
	private final LinkedList<Segment> rcv_queue = new LinkedList<>();
	private final LinkedList<Segment> snd_buf = new LinkedList<>();
	private final LinkedList<Segment> rcv_buf = new LinkedList<>();
	private final LinkedList<Integer> acklist = new LinkedList<>();
	private ByteBuf buffer;
	private int fastresend;
	private int nocwnd;
	private boolean stream;// 流模式
	private final Output output;
	private final Object user;// 远端地址
	private long nextUpdate;// the next update time.

	private static int _ibound_(int lower, int middle, int upper) {
		return Math.min(Math.max(lower, middle), upper);
	}

	private static int _itimediff(int later, int earlier) {
		return later - earlier;
	}

	private static long _itimediff(long later, long earlier) {
		return later - earlier;
	}

	/**
	 * SEGMENT，分片内部内
	 */
	class Segment {

		private int conv = 0;
		private byte cmd = 0;
		private int frg = 0;
		private int wnd = 0;
		private int ts = 0;
		private int sn = 0;
		private int una = 0;
		private int resendts = 0;
		private int rto = 0;
		private int fastack = 0;
		private int xmit = 0;
		private ByteBuf data;

		private Segment(int size) {
			if (size > 0) {
				this.data = PooledByteBufAllocator.DEFAULT.buffer(size);
			}
		}

		/**
		 * encode a segment into buffer
		 *
		 * @param buf
		 * @return
		 */
		private int encode(ByteBuf buf) {
			int off = buf.writerIndex();
			buf.writeIntLE(conv);
			buf.writeByte(cmd);
			buf.writeByte(frg);
			buf.writeShortLE(wnd);
			buf.writeIntLE(ts);
			buf.writeIntLE(sn);
			buf.writeIntLE(una);
			buf.writeIntLE(data == null ? 0 : data.readableBytes());
			return buf.writerIndex() - off;
		}

		/**
		 * 释放内存
		 */
		private void release() {
			if (this.data != null && data.refCnt() > 0) {
				this.data.release(data.refCnt());
			}
		}
	}

	/**
	 * create a new kcpcb
	 *
	 * @param output
	 * @param user
	 */
	public Kcp(Output output, Object user) {
		snd_wnd = IKCP_WND_SND;
		rcv_wnd = IKCP_WND_RCV;
		rmt_wnd = IKCP_WND_RCV;
		mtu = IKCP_MTU_DEF;
		mss = mtu - IKCP_OVERHEAD;
		rx_rto = IKCP_RTO_DEF;
		rx_minrto = IKCP_RTO_MIN;
		interval = IKCP_INTERVAL;
		ts_flush = IKCP_INTERVAL;
		ssthresh = IKCP_THRESH_INIT;
		dead_link = IKCP_DEADLINK;
		buffer = PooledByteBufAllocator.DEFAULT.buffer((mtu + IKCP_OVERHEAD) * 3);
		this.output = output;
		this.user = user;
	}

	/**
	 * check the size of next message in the recv queue
	 *
	 * @return
	 */
	public int peekSize() {
		if (rcv_queue.isEmpty()) {
			return -1;
		}
		Segment seq = rcv_queue.getFirst();
		if (seq.frg == 0) {
			return seq.data.readableBytes();
		}
		if (rcv_queue.size() < seq.frg + 1) {
			return -1;
		}
		int length = 0;
		for (Segment item : rcv_queue) {
			length += item.data.readableBytes();
			if (item.frg == 0) {
				break;
			}
		}
		return length;
	}

	/**
	 * user/upper level recv: returns size, returns below zero for EAGAIN
	 *
	 * @param buffer
	 * @return
	 */
	public int receive(ByteBuf buffer) {
		if (rcv_queue.isEmpty()) {
			return -1;
		}
		int peekSize = peekSize();
		if (peekSize < 0) {
			return -2;
		}
		boolean recover = rcv_queue.size() >= rcv_wnd;
		// merge fragment.
		int c = 0;
		int len = 0;
		for (Segment seg : rcv_queue) {
			len += seg.data.readableBytes();
			buffer.writeBytes(seg.data);
			c++;
			if (seg.frg == 0) {
				break;
			}
		}
		if (c > 0) {
			for (int i = 0; i < c; i++) {
				rcv_queue.removeFirst().data.release();
			}
		}
		if (len != peekSize) {
			throw new RuntimeException("数据异常.");
		}
		// move available data from rcv_buf -> rcv_queue
		c = 0;
		for (Segment seg : rcv_buf) {
			if (seg.sn == rcv_nxt && rcv_queue.size() < rcv_wnd) {
				rcv_queue.add(seg);
				rcv_nxt++;
				c++;
			} else {
				break;
			}
		}
		if (c > 0) {
			for (int i = 0; i < c; i++) {
				rcv_buf.removeFirst();
			}
		}
		// fast recover
		if (rcv_queue.size() < rcv_wnd && recover) {
			// ready to send back IKCP_CMD_WINS in ikcp_flush
			// tell remote my window size
			probe |= IKCP_ASK_TELL;
		}
		return len;
	}

	/**
	 * user/upper level send, returns below zero for error
	 *
	 * @param buffer
	 * @return
	 */
	public int send(ByteBuf buffer) {
		if (buffer.readableBytes() == 0) // 可读区大小=0，无数据
		{
			return -1;
		}
		// append to previous segment in streaming mode (if possible)
		// 启动流模式下 拼接分片
		if (this.stream && !this.snd_queue.isEmpty()) // 流模式并且发送队列不为空
		{
			Segment seg = snd_queue.getLast(); // 队列最后一个，先进先出
			if (seg.data != null && seg.data.readableBytes() < mss) // 不为空且 可读区域小于最大分片大小
			{
				// ByteBuf 内存模型解释文章 https://my.oschina.net/7001/blog/742236
				int capacity = mss - seg.data.readableBytes(); // 可读区域大小与最小分片大小的差值
				int extend = (buffer.readableBytes() < capacity) ? buffer.readableBytes() : capacity;
				seg.data.writeBytes(buffer, extend);
				if (buffer.readableBytes() == 0) // 可读区大小=0，无数据
				{
					return 0;
				}
			}
		}
		// 根据发送数据大小，通过和mss大小比较，计算发送消息需要的分片数量
		int count;
		if (buffer.readableBytes() <= mss)// 可读区<=最大分片大小
		{
			count = 1; // 分片数量
		} else {
			count = (buffer.readableBytes() + mss - 1) / mss;
		}
		if (count > 255) // 分片数量过多
		{
			return -2; // ？？数据过大？
		}
		if (count == 0) {
			count = 1; // 最少分片分片数量1个
		}
		// fragment 碎片，将数据化成几个分片，再将分片加入发送队列中
		for (int i = 0; i < count; i++) {
			int size = buffer.readableBytes() > mss ? mss : buffer.readableBytes();
			Segment seg = new Segment(size);// 为分片的打他分配size大小的内存空间
			seg.data.writeBytes(buffer, size);// 内存空间写size大小数据
			seg.frg = this.stream ? 0 : count - i - 1;// 流模式下 frg=0，否则frg=count-1-i
			snd_queue.add(seg);// 分片加入发送队列
		}
		buffer.release(); // 释放ByteBuf内存空间
		return 0; // 返回0成功，表示发送成功 ，其他值异常
	}

	/**
	 * update ack. 更新ACK
	 *
	 * @param rtt
	 */
	private void update_ack(int rtt) // rtt是 报文收到回应与发送时的时间差
	{
		// 第一次测量，rtt 是我们测量的结果，rx_srtt = rtt，rx_rttval = rtt / 2
		if (rx_srtt == 0) // rx_srtt初始为0时
		{
			rx_srtt = rtt;
			rx_rttval = rtt / 2;
		}
		/*
		 * 以后每次测量： rx_srtt =(1-a) * rx_srtt + a * rtt，a取值1/8 rx_rttval= (1-b) *
		 * rx_rttval + b * |rtt - rx_srtt|，b取值1/4 rto = rx_srtt + 4 * rx_rttval rx_rto =
		 * MIN(MAX(rx_minrto, rto), IKCP_RTO_MAX)
		 * 原文：https://blog.csdn.net/yongkai0214/article/details/85212831
		 */
		else // rx_srtt 已经有值时
		{
			int delta = rtt - rx_srtt;
			if (delta < 0) {
				delta = -delta;
			}
			rx_rttval = (3 * rx_rttval + delta) / 4;
			rx_srtt = (7 * rx_srtt + rtt) / 8;
			if (rx_srtt < 1) // rx_srtt>=1
			{
				rx_srtt = 1;
			}
		}
		int rto = rx_srtt + Math.max(interval, 4 * rx_rttval); // 计算重传超时时间
		rx_rto = _ibound_(rx_minrto, rto, IKCP_RTO_MAX); // 由ACK接受延迟计算出来的 重传超时时间
	}

	// 缩小缓冲区 ，更新snd_una为snd_buf中seg.sn或snd.nxt
	private void shrink_buf() {
		if (snd_buf.size() > 0) // 缓冲区大于0
		{
			snd_una = snd_buf.getFirst().sn;// 获取第一个分片的编号作为kcp包中下一个确认的kcp包序列号
		} else // 缓冲区等于0
		{
			snd_una = snd_nxt; // 下一个发送的kcp包序列
		}
	}

	// 解析ACk
	// 遍历snd_buf中（snd_una, snd_nxt），将sn相等的删除，直到大于sn
	// ack报文则包含了对端收到的kcp包的序号，接到ack包后需要删除发送缓冲区中与ack包中的发送包序号（sn）相同的kcp包。
	private void parse_ack(int sn) {
		// kcp报文的una字段（snd_una：第一个未确认的包）表示对端希望接收的下一个kcp包序号
		// ack报文编号sn小于第一个未确认的包，或者ack报文编号sn大于等于下一个待分配包的序列号
		if (_itimediff(sn, snd_una) < 0 || _itimediff(sn, snd_nxt) >= 0) // ？？不懂
		{
			return;
		}
		for (int i = 0; i < snd_buf.size(); i++) //
		{
			Segment seg = snd_buf.get(i);
			if (sn == seg.sn) {
				snd_buf.remove(i);// 接到ack包后需要删除发送缓冲区中与ack包中的发送包序号（sn）相同的kcp包。
				seg.data.release(seg.data.refCnt());
				break;
			}
			if (_itimediff(sn, seg.sn) < 0) // ？？不懂
			{
				break;
			}
		}
	}

	// 解析una字段后需要把发送缓冲区里面包序号小于una的包全部丢弃掉
	private void parse_una(int una) // 解析下一个还未确认的报文编号
	{
		int c = 0;
		for (Segment seg : snd_buf) // 遍历分片
		{
			if (_itimediff(una, seg.sn) > 0) // 下一个可接受的序列号una大于已经收到的序列号sn，可以理解成找出该分片之前有几个已经确认成功的分片
			{
				c++;
			} else {
				break;
			}
		}
		if (c > 0) // 把确认接收成功的分片从集合中删除
		{
			for (int i = 0; i < c; i++) {
				Segment seg = snd_buf.removeFirst();// 删除LinkedList的第一个元素，并且获得该元素
				seg.data.release(seg.data.refCnt());// 释放删除的分片占用的空间
			}
		}
	}

	// sn 大于snd_buf中包序号，可能有丢包发生
	private void parse_fastack(int sn) {
		if (_itimediff(sn, snd_una) < 0 || _itimediff(sn, snd_nxt) >= 0) {
			return;
		}
		for (Segment seg : this.snd_buf) {
			if (_itimediff(sn, seg.sn) < 0) {
				break;
			} else if (sn != seg.sn) {
				seg.fastack++;
			}
		}
	}

	/**
	 * ack append 更新segment的sn及ts放在acklist中
	 * 
	 * @param sn
	 * @param ts
	 */
	private void ack_push(int sn, int ts) {
		acklist.add(sn);
		acklist.add(ts);
	}

	private void parse_data(Segment newseg) {
		int sn = newseg.sn;
		if (_itimediff(sn, rcv_nxt + rcv_wnd) >= 0 || _itimediff(sn, rcv_nxt) < 0) {
			newseg.release();
			return;
		}
		int n = rcv_buf.size() - 1;
		int temp = -1;
		boolean repeat = false;
		for (int i = n; i >= 0; i--) {
			Segment seg = rcv_buf.get(i);
			if (seg.sn == sn) {
				repeat = true;
				break;
			}
			if (_itimediff(sn, seg.sn) > 0) {
				temp = i;
				break;
			}
		}
		if (!repeat) {
			if (temp == -1) {
				rcv_buf.addFirst(newseg);
			} else {
				rcv_buf.add(temp + 1, newseg);
			}
		} else {
			newseg.release();
		}
		// move available data from rcv_buf -> rcv_queue
		int c = 0;
		for (Segment seg : rcv_buf) {
			if (seg.sn == rcv_nxt && rcv_queue.size() < rcv_wnd) {
				rcv_queue.add(seg);
				rcv_nxt++;
				c++;
			} else {
				break;
			}
		}
		if (0 < c) {
			for (int i = 0; i < c; i++) {
				rcv_buf.removeFirst();
			}
		}
	}

	/**
	 *
	 * when you received a low level packet (eg. UDP packet), call it
	 *
	 * @param data
	 * @return
	 */
	// 收到底层来的UPD数据包，将他解析
	public int input(ByteBuf data) {
		int una_temp = snd_una;// 下一个确定的kcp包序列
		int flag = 0, maxack = 0;
		if (data == null || data.readableBytes() < IKCP_OVERHEAD)// ？？
		{
			return -1;
		}
		while (true) // 循环
		{
			boolean readed = false;
			// 以下八个参数是kcp header
			int ts;// 时间序列
			int sn;// 序列号
			int len;// 数据长度
			int una;// 下一个可接受的序列号，也就是确认号，收到sn=10，una=sn+1
			int conv_;// 连接号，用于表示哪一个客户端
			int wnd;// 接收窗口大小，发送窗口不能超过接收方给出的数据
			byte cmd;// 命令字，如IKCP_CMD_ACK 等，有四个
			byte frg;// 分片
			if (data.readableBytes() < IKCP_OVERHEAD)// 判断data中刻度字节大小，IKCP_OVERHEAD=24
			{
				break; // 跳出while循环
			}
			conv_ = data.readIntLE();// 取连接号？？？，
			if (this.conv != conv_) // 判断当前的客户端连接号是否为传入的客户端连接号
			{
				return -1; // 代表什么？
			}
			cmd = data.readByte(); // data数据如何存放是个问题?? byte 一字节八位
			frg = data.readByte();// 每读一次，指针往后移
			wnd = data.readShortLE(); // 2字节 16位
			ts = data.readIntLE(); // 4字节32位
			sn = data.readIntLE(); // 4字节32位
			una = data.readIntLE();// 4字节32位
			len = data.readIntLE();// 4字节32位 len表示数据长度
			if (data.readableBytes() < len) // data的可读字节小于 len 数据长度
			{
				return -2;// 代表什么？
			}
			switch ((int) cmd) // 判断 命令字是哪一个
			{
			// IKCP_CMD_PUSH = 81 // cmd: push data，数据包
			// IKCP_CMD_ACK = 82 // cmd: ack，确认包，告诉对方收到数据包
			// IKCP_CMD_WASK = 83 // cmd: window probe (ask)，询问远端滑动窗口的大小
			// IKCP_CMD_WINS = 84 // cmd: window size (tell)，告知远端滑动窗口的大小
			case IKCP_CMD_PUSH:// 传输数据包
			case IKCP_CMD_ACK: // 确认命令
			case IKCP_CMD_WASK: // 接收窗口大小询问命令
			case IKCP_CMD_WINS: // 接受窗口大小告知命令
				break;
			default:
				return -3; // 代表什么？
			}
			rmt_wnd = wnd & 0x0000ffff;// 远程窗口大小 1111 1111 1111 1111 2字节 16位
			// kcp报文的una字段（snd_una：第一个未确认的包）表示对端希望接收的下一个kcp包序号，
			// 也就是说明接收端已经收到了所有小于una序号的kcp包。解析una字段后需要把发送缓冲区里面包序号小于una的包全部丢弃掉。
			parse_una(una); // 解析下一个还未确认的报文编号，删除小于snd_buf中小于una的segment
			shrink_buf();// 缩小缓冲区,更新snd_una为snd_buf中seg.sn或snd.nxt
			switch (cmd) // 判断 命令字是哪一个
			{
			case IKCP_CMD_ACK: // 确认命令ack报文
				if (_itimediff(current, ts) >= 0) // 报文收到回应时间与发送时间差>=0
				{
					// 更新rx_srtt，rx_rttval，计算rx_rto
					update_ack(_itimediff(current, ts)); // 调用update_ack 来根据 ACK 时间戳更新本地的 rtt
				}
				// 遍历snd_buf中（snd_una, snd_nxt），将sn相等的删除，直到大于sn
				parse_ack(sn); // 解析ACK， 更新 rtt
				shrink_buf(); // 缩小缓冲区，// 更新控制块的 snd_una
				if (flag == 0) {
					flag = 1;// 快速重传标记
					maxack = sn; // 最大ack=ack报文的确认编号，// 记录最大的 ACK 编号
				} else if (_itimediff(sn, maxack) > 0) {
					maxack = sn; // 记录最大的 ACK 编号
				}
				break;
			case IKCP_CMD_PUSH:// 是数据报文
				// 需要判断数据报文是否在接收窗口内，如果是则保存ack，如果数据报文的sn正好是待接收的第一个报文rcv_nxt，
				// 那么就更新rcv_nxt(加1)。如果配置了ackNodelay模式（无延迟ack）或者远端窗口为0（代表暂时不能发送用户数据），
				// 那么这里会立刻fulsh（）发送ack。
				// 原文：https://blog.csdn.net/qq_36748278/article/details/80171575
				if (_itimediff(sn, rcv_nxt + rcv_wnd) < 0)// 需要判断数据报文是否在接收窗口内，如果是
				{
					ack_push(sn, ts);// 保存ack？？？//更新segment的sn及ts放在acklist中
					if (_itimediff(sn, rcv_nxt) >= 0) {
						Segment seg = new Segment(len);
						seg.conv = conv_;
						seg.cmd = cmd;
						seg.frg = frg & 0x000000ff;
						seg.wnd = wnd;
						seg.ts = ts;
						seg.sn = sn;
						seg.una = una;
						if (len > 0) {
							seg.data.writeBytes(data, len);
							readed = true;
						}
						// 1. 丢弃sn > kcp->rcv_nxt + kcp->rcv_wnd的segment;
						// 2. 逐一比较rcv_buf中的segment，若重复丢弃，非重复，新建segment加入;
						// 3. 检查rcv_buf的包序号sn，如果是待接收的序号rcv_nxt，且可以接收（接收队列小 于接收窗口），
						// 转移segment到rcv_buf，nrcv_buf减少，nrcv_que增加，rcv_nxt增加;
						parse_data(seg);
					}
				}
				break;
			case IKCP_CMD_WASK:
				// ready to send back IKCP_CMD_WINS in Ikcp_flush
				// tell remote my window size
				probe |= IKCP_ASK_TELL;// 0000 0000 0000 0010 2 告知远端窗口大小
				break;
			case IKCP_CMD_WINS:
				// do nothing
				break;
			default:
				return -3; // ？？
			}
			if (!readed) {
				data.skipBytes(len);
			}
		}
		if (flag != 0) {
			parse_fastack(maxack);// sn大于snd_buf中包序号，可能有丢包发生
		}
		// 如果snd_una增加了那么就说明对端正常收到且回应了发送方发送缓冲区第一个待确认的包，此时需要更新cwnd（拥塞窗口）
		if (_itimediff(snd_una, una_temp) > 0) {
			if (this.cwnd < this.rmt_wnd) {
				if (this.cwnd < this.ssthresh) {
					this.cwnd++;
					this.incr += mss;
				} else {
					if (this.incr < mss) {
						this.incr = mss;
					}
					this.incr += (mss * mss) / this.incr + (mss / 16);
					if ((this.cwnd + 1) * mss <= this.incr) {
						this.cwnd++;
					}
				}
				if (this.cwnd > this.rmt_wnd) {
					this.cwnd = this.rmt_wnd;
					this.incr = this.rmt_wnd * mss;
				}
			}
		}
		return 0;
	}

	private int wnd_unused() {
		if (rcv_queue.size() < rcv_wnd) {
			return rcv_wnd - rcv_queue.size();
		}
		return 0;
	}

	/**
	 * force flush
	 */
	public void forceFlush() {
		int cur = current;
		int change = 0;
		int lost = 0;
		Segment seg = new Segment(0);
		seg.conv = conv;
		seg.cmd = IKCP_CMD_ACK;
		seg.wnd = wnd_unused();
		seg.una = rcv_nxt;
		// flush acknowledges
		int c = acklist.size() / 2;
		for (int i = 0; i < c; i++) {
			if (buffer.readableBytes() + IKCP_OVERHEAD > mtu) {
				this.output.out(buffer, this, user);
				buffer = PooledByteBufAllocator.DEFAULT.buffer((mtu + IKCP_OVERHEAD) * 3);
			}
			seg.sn = acklist.get(i * 2 + 0);
			seg.ts = acklist.get(i * 2 + 1);
			seg.encode(buffer);
		}
		acklist.clear();
		// probe window size (if remote window size equals zero)
		if (rmt_wnd == 0) {
			if (probe_wait == 0) {
				probe_wait = IKCP_PROBE_INIT;
				ts_probe = current + probe_wait;
			} else if (_itimediff(current, ts_probe) >= 0) {
				if (probe_wait < IKCP_PROBE_INIT) {
					probe_wait = IKCP_PROBE_INIT;
				}
				probe_wait += probe_wait / 2;
				if (probe_wait > IKCP_PROBE_LIMIT) {
					probe_wait = IKCP_PROBE_LIMIT;
				}
				ts_probe = current + probe_wait;
				probe |= IKCP_ASK_SEND;
			}
		} else {
			ts_probe = 0;
			probe_wait = 0;
		}
		// flush window probing commands
		if ((probe & IKCP_ASK_SEND) != 0) {
			seg.cmd = IKCP_CMD_WASK;
			if (buffer.readableBytes() + IKCP_OVERHEAD > mtu) {
				this.output.out(buffer, this, user);
				buffer = PooledByteBufAllocator.DEFAULT.buffer((mtu + IKCP_OVERHEAD) * 3);
			}
			seg.encode(buffer);
		}
		// flush window probing commands
		if ((probe & IKCP_ASK_TELL) != 0) {
			seg.cmd = IKCP_CMD_WINS;
			if (buffer.readableBytes() + IKCP_OVERHEAD > mtu) {
				this.output.out(buffer, this, user);
				buffer = PooledByteBufAllocator.DEFAULT.buffer((mtu + IKCP_OVERHEAD) * 3);
			}
			seg.encode(buffer);
		}
		probe = 0;
		// calculate window size
		int cwnd_temp = Math.min(snd_wnd, rmt_wnd);
		if (nocwnd == 0) {
			cwnd_temp = Math.min(cwnd, cwnd_temp);
		}
		// move data from snd_queue to snd_buf
		c = 0;
		for (Segment item : snd_queue) {
			if (_itimediff(snd_nxt, snd_una + cwnd_temp) >= 0) {
				break;
			}
			Segment newseg = item;
			newseg.conv = conv;
			newseg.cmd = IKCP_CMD_PUSH;
			newseg.wnd = seg.wnd;
			newseg.ts = cur;
			newseg.sn = snd_nxt++;
			newseg.una = rcv_nxt;
			newseg.resendts = cur;
			newseg.rto = rx_rto;
			newseg.fastack = 0;
			newseg.xmit = 0;
			snd_buf.add(newseg);
			c++;
		}
		if (c > 0) {
			for (int i = 0; i < c; i++) {
				snd_queue.removeFirst();
			}
		}
		// calculate resent
		int resent = (fastresend > 0) ? fastresend : Integer.MAX_VALUE;
		int rtomin = (nodelay == 0) ? (rx_rto >> 3) : 0;
		// flush data segments
		for (Segment segment : snd_buf) {
			boolean needsend = false;
			if (segment.xmit == 0) {
				needsend = true;
				segment.xmit++;
				segment.rto = rx_rto;
				segment.resendts = cur + segment.rto + rtomin;
			} else if (_itimediff(cur, segment.resendts) >= 0) {
				needsend = true;
				segment.xmit++;
				xmit++;
				if (nodelay == 0) {
					segment.rto += rx_rto;
				} else {
					segment.rto += rx_rto / 2;
				}
				segment.resendts = cur + segment.rto;
				lost = 1;
			} else if (segment.fastack >= resent) {
				needsend = true;
				segment.xmit++;
				segment.fastack = 0;
				segment.resendts = cur + segment.rto;
				change++;
			}
			if (needsend) {
				segment.ts = cur;
				segment.wnd = seg.wnd;
				segment.una = rcv_nxt;
				int need = IKCP_OVERHEAD + segment.data.readableBytes();
				if (buffer.readableBytes() + need > mtu) {
					this.output.out(buffer, this, user);
					buffer = PooledByteBufAllocator.DEFAULT.buffer((mtu + IKCP_OVERHEAD) * 3);
				}
				segment.encode(buffer);
				if (segment.data.readableBytes() > 0) {
					buffer.writeBytes(segment.data.duplicate());
				}
				if (segment.xmit >= dead_link) {
					state = -1;
				}
			}
		}
		// flash remain segments
		if (buffer.readableBytes() > 0) {
			this.output.out(buffer, this, user);
			buffer = PooledByteBufAllocator.DEFAULT.buffer((mtu + IKCP_OVERHEAD) * 3);
		}
		// update ssthresh
		if (change != 0) {
			int inflight = snd_nxt - snd_una;
			ssthresh = inflight / 2;
			if (ssthresh < IKCP_THRESH_MIN) {
				ssthresh = IKCP_THRESH_MIN;
			}
			cwnd = ssthresh + resent;
			incr = cwnd * mss;
		}
		if (lost != 0) {
			ssthresh = cwnd / 2;
			if (ssthresh < IKCP_THRESH_MIN) {
				ssthresh = IKCP_THRESH_MIN;
			}
			cwnd = 1;
			incr = mss;
		}
		if (cwnd < 1) {
			cwnd = 1;
			incr = mss;
		}
	}

	/**
	 * flush pending data
	 */
	public void flush() {
		if (updated != 0) {
			forceFlush();
		}
	}

	/**
	 * update state (call it repeatedly, every 10ms-100ms), or you can ask
	 * ikcp_check when to call it again (without ikcp_input/_send calling).
	 *
	 * @param current current timestamp in millisec.
	 */
	public void update(long current) {
		this.current = (int) current;
		if (updated == 0) {
			updated = 1;
			ts_flush = this.current;
		}
		int slap = _itimediff(this.current, ts_flush);
		if (slap >= 10000 || slap < -10000) {
			ts_flush = this.current;
			slap = 0;
		}
		if (slap >= 0) {
			ts_flush += interval;
			if (_itimediff(this.current, ts_flush) >= 0) {
				ts_flush = this.current + interval;
			}
			flush();
		}
	}

	/**
	 * Determine when should you invoke ikcp_update: returns when you should invoke
	 * ikcp_update in millisec, if there is no ikcp_input/_send calling. you can
	 * call ikcp_update in that time, instead of call update repeatly. Important to
	 * reduce unnacessary ikcp_update invoking. use it to schedule ikcp_update (eg.
	 * implementing an epoll-like mechanism, or optimize ikcp_update when handling
	 * massive kcp connections)
	 *
	 * @param current
	 * @return
	 */
	public long check(long current) {
		long cur = current;
		if (updated == 0) {
			return cur;
		}
		long ts_flush_temp = this.ts_flush;
		long tm_packet = 0x7fffffff;
		if (_itimediff(cur, ts_flush_temp) >= 10000 || _itimediff(cur, ts_flush_temp) < -10000) {
			ts_flush_temp = cur;
		}
		if (_itimediff(cur, ts_flush_temp) >= 0) {
			return cur;
		}
		long tm_flush = _itimediff(ts_flush_temp, cur);
		for (Segment seg : snd_buf) {
			long diff = _itimediff(seg.resendts, cur);
			if (diff <= 0) {
				return cur;
			}
			if (diff < tm_packet) {
				tm_packet = diff;
			}
		}
		long minimal = tm_packet < tm_flush ? tm_packet : tm_flush;
		if (minimal >= interval) {
			minimal = interval;
		}
		return cur + minimal;
	}

	/**
	 * change MTU size, default is 1400
	 *
	 * @param mtu
	 * @return
	 */
	public int setMtu(int mtu) {
		if (mtu < 50 || mtu < IKCP_OVERHEAD) {
			return -1;
		}
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer((mtu + IKCP_OVERHEAD) * 3);
		this.mtu = mtu;
		mss = mtu - IKCP_OVERHEAD;
		if (buffer != null) {
			buffer.release();
		}
		this.buffer = buf;
		return 0;
	}

	/**
	 * conv
	 *
	 * @param conv
	 */
	public void setConv(int conv) {
		this.conv = conv;
	}

	/**
	 * conv
	 *
	 * @return
	 */
	public int getConv() {
		return conv;
	}

	/**
	 * interval per update
	 *
	 * @param interval
	 * @return
	 */
	public int interval(int interval) {
		if (interval > 5000) {
			interval = 5000;
		} else if (interval < 10) {
			interval = 10;
		}
		this.interval = interval;
		return 0;
	}

	/**
	 * fastest: ikcp_nodelay(kcp, 1, 20, 2, 1) nodelay: 0:disable(default), 1:enable
	 * interval: internal update timer interval in millisec, default is 100ms
	 * resend: 0:disable fast resend(default), 1:enable fast resend nc: 0:normal
	 * congestion control(default), 1:disable congestion control
	 *
	 * @param nodelay
	 * @param interval
	 * @param resend
	 * @param nc
	 * @return
	 */
	public int noDelay(int nodelay, int interval, int resend, int nc) {
		if (nodelay >= 0) {
			this.nodelay = nodelay;
			if (nodelay != 0) {
				rx_minrto = IKCP_RTO_NDL;
			} else {
				rx_minrto = IKCP_RTO_MIN;
			}
		}
		if (interval >= 0) {
			if (interval > 5000) {
				interval = 5000;
			} else if (interval < 10) {
				interval = 10;
			}
			this.interval = interval;
		}
		if (resend >= 0) {
			fastresend = resend;
		}
		if (nc >= 0) {
			nocwnd = nc;
		}
		return 0;
	}

	/**
	 * set maximum window size: sndwnd=32, rcvwnd=32 by default
	 *
	 * @param sndwnd
	 * @param rcvwnd
	 * @return
	 */
	public int wndSize(int sndwnd, int rcvwnd) {
		if (sndwnd > 0) {
			snd_wnd = sndwnd;
		}
		if (rcvwnd > 0) {
			rcv_wnd = rcvwnd;
		}
		return 0;
	}

	/**
	 * get how many packet is waiting to be sent
	 *
	 * @return
	 */
	public int waitSnd() {
		return snd_buf.size() + snd_queue.size();
	}

	public void setNextUpdate(long nextUpdate) {
		this.nextUpdate = nextUpdate;
	}

	public long getNextUpdate() {
		return nextUpdate;
	}

	public Object getUser() {
		return user;
	}

	public boolean isStream() {
		return stream;
	}

	public void setStream(boolean stream) {
		this.stream = stream;
	}

	public void setMinRto(int min) {
		rx_minrto = min;
	}

	@Override
	public String toString() {
		return this.user.toString();
	}

	/**
	 * 释放内存
	 */
	void release() {
		if (buffer.refCnt() > 0) {
			this.buffer.release(buffer.refCnt());
		}
		for (Segment seg : this.rcv_buf) {
			seg.release();
		}
		for (Segment seg : this.rcv_queue) {
			seg.release();
		}
		for (Segment seg : this.snd_buf) {
			seg.release();
		}
		for (Segment seg : this.snd_queue) {
			seg.release();
		}
	}
}
