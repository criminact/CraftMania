package org.craftmania.world;

import org.craftmania.utilities.ThreadPool;

public class BlockChunkThreading
{

	private ChunkManager _chunkManager;
	private ThreadPool _generatePool;
	private ThreadPool _savePool;
	private ThreadPool _deletePool;
	private volatile int _threads;

	public BlockChunkThreading(ChunkManager chman)
	{
		_chunkManager = chman;
		_generatePool = new ThreadPool(1);
		_savePool = new ThreadPool(1);
		_deletePool = new ThreadPool(1);
	}

	public void saveChunk(final BlockChunk chunk)
	{
		_savePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						_chunkManager.getBlockChunkLoader().saveChunk(chunk);
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				--_threads;
			}
		});
	}
	
	public void deleteChunk(final BlockChunk chunk)
	{
		chunk.setDestroying(true);
		/* Mesh has to be deleted in the main thread, because of OpenGL */
		chunk.destroyMesh();
		
		/* Add a runnable to the pool */
		_deletePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						chunk.destroy();
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				--_threads;
			}
		});
	}
	
	public void loadChunk(final BlockChunk chunk)
	{
		chunk.setLoading(true);
		_generatePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						_chunkManager.getBlockChunkLoader().loadChunk(chunk);
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				--_threads;
			}
		});
	}

	public void generateChunk(final BlockChunk chunk)
	{
		chunk.setLoading(true);
		_generatePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						chunk.generate();
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				--_threads;
			}
		});
	}

	public void saveAndUnloadChunk(final BlockChunk chunk)
	{
		/* Mesh has to be deleted in the main thread, because of OpenGL */
		chunk.destroyMesh();
		
		/* Add a runnable to the pool */
		_savePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						_chunkManager.getBlockChunkLoader().saveChunk(chunk);
						chunk.destroy();
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				--_threads;
			}
		});
	}

	public boolean isTreadingBusy()
	{
		return _threads > 0;
	}

	public void loadAndGenerateChunk(final BlockChunk chunk)
	{
		chunk.setLoading(true);
		_generatePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						_chunkManager.getBlockChunkLoader().loadChunk(chunk);
						chunk.generate();
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				--_threads;
			}
		});
	}
}
