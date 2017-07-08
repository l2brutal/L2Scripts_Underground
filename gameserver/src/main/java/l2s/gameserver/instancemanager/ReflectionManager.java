package l2s.gameserver.instancemanager;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import l2s.gameserver.data.xml.holder.DoorHolder;
import l2s.gameserver.data.xml.holder.ZoneHolder;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.utils.Location;

public class ReflectionManager
{
	public static final Reflection DEFAULT = Reflection.createReflection(0);
	public static final Reflection PARNASSUS = Reflection.createReflection(-1);
	public static final Reflection GIRAN_HARBOR = Reflection.createReflection(-2);
	public static final Reflection JAIL = Reflection.createReflection(-3);
	public static final Reflection CTF_EVENT = Reflection.createReflection(-4);  //Добавлено
	public static final Reflection TVT_EVENT = Reflection.createReflection(-5);//Добавлено
	public static final Reflection LAST_HERO = Reflection.createReflection(-6);//Добавлено	
	public static final int FIGHT_CLUB_REFLECTION_ID = 400;

	private static final ReflectionManager _instance = new ReflectionManager();

	public static ReflectionManager getInstance()
	{
		return _instance;
	}

	private final TIntObjectHashMap<Reflection> _reflections = new TIntObjectHashMap<Reflection>();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	private ReflectionManager()
	{
		//
	}

	public void init()
	{
		add(DEFAULT);
		add(PARNASSUS);
		add(GIRAN_HARBOR);
		add(JAIL);
		add(TVT_EVENT);//Добавлено
		add(CTF_EVENT);//Добавлено
		add(LAST_HERO);//Добавлено

		// создаем в рефлекте все зоны, и все двери
		DEFAULT.init(DoorHolder.getInstance().getDoors(), ZoneHolder.getInstance().getZones());

		Map<String, ZoneTemplate> _dummy = new HashMap<String, ZoneTemplate>();

		TVT_EVENT.init(DoorHolder.getInstance().getDoors(), _dummy);
		CTF_EVENT.init(DoorHolder.getInstance().getDoors(), _dummy);
		LAST_HERO.init(DoorHolder.getInstance().getDoors(), _dummy);

		JAIL.setCoreLoc(new Location(-114648, -249384, -2984));
	}

	public Reflection get(int id)
	{
		readLock.lock();
		try
		{
			return _reflections.get(id);
		}
		finally
		{
			readLock.unlock();
		}
	}

	public Reflection add(Reflection ref)
	{
		writeLock.lock();
		try
		{
			return _reflections.put(ref.getId(), ref);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public Reflection remove(Reflection ref)
	{
		writeLock.lock();
		try
		{
			return _reflections.remove(ref.getId());
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public Reflection[] getAll()
	{
		readLock.lock();
		try
		{
			return _reflections.values(new Reflection[_reflections.size()]);
		}
		finally
		{
			readLock.unlock();
		}
	}

	public int getCountByIzId(int izId)
	{
		readLock.lock();
		try
		{
			int i = 0;
			for(Reflection r : getAll())
				if(r.getInstancedZoneId() == izId)
					i++;
			return i;
		}
		finally
		{
			readLock.unlock();
		}
	}

	public int size()
	{
		return _reflections.size();
	}
}