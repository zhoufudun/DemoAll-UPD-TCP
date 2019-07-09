package com;


public abstract class TestkcpClient implements TestOutput, TestKcpListerner, Runnable {
	
	public TestkcpClient()
	{
	     this(0);
	}
	public TestkcpClient(int port) {
		System.out.println(port);
	}
	@Override
	public void run() {
		
	}

	@Override
	public void handleReceive() {
		
	}

	@Override
	public void out() {
		
	}
	
	
}
