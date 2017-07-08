package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.text.StrTable;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.model.instances.FakePlayerInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Общее "супер" хранилище для всех объектов L2Object,
 * объекты делятся на типы - максимум 32 (0 - 31), для каждого типа свое хранилище,
 * в каждом хранилище может хранится до 67 108 864 объектов (0 - 67108863),
 * каждому объекту назначается уникальный 64-битный индентификатор типа long
 * который бинарно складывается из objId + тип + индекс в хранилище
 * 
 * @author Drin & Diamond
 * 
 */
//TODO [G1ta0] вынести эту бредятину к чертям
@SuppressWarnings("rawtypes")
public class GameObjectsStorage
{
	@SuppressWarnings("unused")
	private static final Logger _log = LoggerFactory.getLogger(GameObjectsStorage.class);

	private static final int STORAGE_PLAYERS = 0x00;
	private static final int STORAGE_SUMMONS = 0x01;
	private static final int STORAGE_NPCS = 0x02;
	private static final int STORAGE_FAKE_PLAYERS = 0x03;
	/** ......................................... */
	private static final int STORAGE_OTHER = 0x1E;
	private static final int STORAGE_NONE = 0x1F;

	private static final GameObjectArray[] storages = new GameObjectArray[STORAGE_NONE];

	static
	{
		storages[STORAGE_PLAYERS] = new GameObjectArray<Player>("PLAYERS", GameServer.getInstance().getOnlineLimit(), 1);
		storages[STORAGE_SUMMONS] = new GameObjectArray<Playable>("SUMMONS", GameServer.getInstance().getOnlineLimit(), 1);
		storages[STORAGE_NPCS] = new GameObjectArray<NpcInstance>("NPCS", 60000 * Config.RATE_MOB_SPAWN, 5000);
		storages[STORAGE_OTHER] = new GameObjectArray<GameObject>("OTHER", 6000, 1000);
		storages[STORAGE_FAKE_PLAYERS] = new GameObjectArray<GameObject>("FAKE_PLAYERS", 1000, 1000);
	}

	@SuppressWarnings("unchecked")
	private static GameObjectArray<Player> getStoragePlayers()
	{
		return storages[STORAGE_PLAYERS];
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private static GameObjectArray<Playable> getStorageSummons()
	{
		return storages[STORAGE_SUMMONS];
	}

	@SuppressWarnings("unchecked")
	private static GameObjectArray<NpcInstance> getStorageNpcs()
	{
		return storages[STORAGE_NPCS];
	}

	@SuppressWarnings("unchecked")
	private static GameObjectArray<FakePlayerInstance> getStorageFakePlayers()
	{
		return storages[STORAGE_FAKE_PLAYERS];
	}

	private static int selectStorageID(GameObject o)
	{
		if(o.isFakePlayer())
			return STORAGE_FAKE_PLAYERS;

		if(o.isNpc())
			return STORAGE_NPCS;

		if(o.isPlayable())
			return o.isPlayer() ? STORAGE_PLAYERS : STORAGE_SUMMONS;

		return STORAGE_OTHER;
	}

	public static GameObject get(long storedId)
	{
		int STORAGE_ID;
		if(storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == STORAGE_NONE)
			return null;
		GameObject result = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return result != null && result.getObjectId() == getStoredObjectId(storedId) ? result : null;
	}

	public static GameObject get(Long storedId)
	{
		int STORAGE_ID;
		if(storedId == null || storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == STORAGE_NONE)
			return null;
		GameObject result = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return result != null && result.getObjectId() == getStoredObjectId(storedId) ? result : null;
	}

	public static boolean isStored(long storedId)
	{
		int STORAGE_ID;
		if(storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == STORAGE_NONE)
			return false;
		GameObject o = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return o != null && o.getObjectId() == getStoredObjectId(storedId);
	}

	public static NpcInstance getAsNpc(long storedId)
	{
		return (NpcInstance) get(storedId);
	}

	public static NpcInstance getAsNpc(Long storedId)
	{
		return (NpcInstance) get(storedId);
	}

	public static Player getAsPlayer(long storedId)
	{
		return (Player) get(storedId);
	}

	public static Playable getAsPlayable(long storedId)
	{
		return (Playable) get(storedId);
	}

	public static Creature getAsCharacter(long storedId)
	{
		return (Creature) get(storedId);
	}

	public static MonsterInstance getAsMonster(long storedId)
	{
		return (MonsterInstance) get(storedId);
	}

	public static PetInstance getAsPet(long storedId)
	{
		return (PetInstance) get(storedId);
	}

	public static ItemInstance getAsItem(long storedId)
	{
		return (ItemInstance) get(storedId);
	}

	public static boolean contains(long storedId)
	{
		return get(storedId) != null;
	}

	public static Player getPlayer(String name)
	{
		return getStoragePlayers().findByName(name);
	}

	public static Player getPlayer(int objId)
	{
		return getStoragePlayers().findByObjectId(objId);
	}

	/**
	 * копия списка игроков из хранилища, пригодна для последующих манипуляций над ней
	 * для перебора в основном лучше использовать getAllPlayersForIterate()
	 */
	public static List<Player> getAllPlayers()
	{
		return getStoragePlayers().getAll();
	}

	/**
	 * использовать только для перебора типа for(L2Player player : getAllPlayersForIterate()) ...
	 */
	public static Iterable<Player> getAllPlayersForIterate()
	{
		return getStoragePlayers();
	}

	public static Player getPlayerByAccount(String account)
	{
		for(Player player : getAllPlayersForIterate())
		{
			if(player.getAccountName().equalsIgnoreCase(account))
				return player;
		}
		return null;
	}

	/**
	 * Возвращает онлайн с офф трейдерами
	 */
	public static int getAllPlayersCount()
	{
		return getStoragePlayers().getRealSize();
	}

	public static int getAllObjectsCount()
	{
		int result = 0;
		for(GameObjectArray<?> storage : storages)
			if(storage != null)
				result += storage.getRealSize();
		return result;
	}

	@SuppressWarnings({ "unchecked" })
	public static List<GameObject> getAllObjects()
	{
		List<GameObject> result = new ArrayList<GameObject>(getAllObjectsCount());
		for(GameObjectArray storage : storages)
			if(storage != null)
				storage.getAll(result);
		return result;
	}

	public static GameObject findObject(int objId)
	{
		GameObject result = null;
		for(GameObjectArray<?> storage : storages)
			if(storage != null)
				if((result = storage.findByObjectId(objId)) != null)
					return result;
		return null;
	}

	private static long offline_refresh = 0;
	private static int offline_count = 0;

	public static int getAllOfflineCount()
	{
		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
			return 0;

		long now = System.currentTimeMillis();
		if(now > offline_refresh)
		{
			offline_refresh = now + 10000;
			offline_count = 0;
			for(Player player : getStoragePlayers())
				if(player.isInOfflineMode())
					offline_count++;
		}

		return offline_count;
	}

	public static List<NpcInstance> getAllNpcs()
	{
		return getStorageNpcs().getAll();
	}

	public static MonsterInstance getRandomMonsterByLevel(int level)
	{
		List<MonsterInstance> monsters = new ArrayList<>();
		for(NpcInstance monster : getAllNpcs())
		{
			if(monster.getLoc() != null && monster.getX() != 0 && monster.getY() != 0 && monster.isMonster() && monster.getLevel() > level - 3 && monster.getLevel() < level + 3)
			{
				monsters.add((MonsterInstance) monster);
			}
		}
		return monsters.get(Rnd.get(monsters.size()));
	}

	/**
	 * использовать только для перебора типа for(L2Player player : getAllPlayersForIterate()) ...
	 */
	public static Iterable<NpcInstance> getAllNpcsForIterate()
	{
		return getStorageNpcs();
	}

	public static NpcInstance getByNpcId(int npc_id)
	{
		NpcInstance result = null;
		for(NpcInstance temp : getStorageNpcs())
			if(npc_id == temp.getNpcId())
			{
				if(!temp.isDead())
					return temp;
				result = temp;
			}
		return result;
	}

	public static List<NpcInstance> getAllByNpcId(int npc_id, boolean justAlive)
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();
		for(NpcInstance temp : getStorageNpcs())
			if(temp.getTemplate() != null && npc_id == temp.getNpcId() && (!justAlive || !temp.isDead()))
				result.add(temp);
		return result;
	}

	public static List<NpcInstance> getAllByNpcId(int[] npc_ids, boolean justAlive)
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();
		for(NpcInstance temp : getStorageNpcs())
			if(!justAlive || !temp.isDead())
				for(int npc_id : npc_ids)
					if(npc_id == temp.getNpcId())
						result.add(temp);
		return result;
	}

	public static NpcInstance getNpc(String s)
	{
		List<NpcInstance> npcs = getStorageNpcs().findAllByName(s);
		if(npcs.size() == 0)
			return null;
		for(NpcInstance temp : npcs)
			if(!temp.isDead())
				return temp;
		if(npcs.size() > 0)
			return npcs.remove(npcs.size() - 1);

		return null;
	}

	public static NpcInstance getNpc(int objId)
	{
		return getStorageNpcs().findByObjectId(objId);
	}

	/**
	 * кладет объект в хранилище и возвращает уникальный индентификатор по которому его можно будет найти в хранилище
	 */
	@SuppressWarnings("unchecked")
	public static long put(GameObject o)
	{
		int STORAGE_ID = selectStorageID(o);
		return o.getObjectId() & 0xFFFFFFFFL | (STORAGE_ID & 0x1FL) << 32 | (storages[STORAGE_ID].add(o) & 0xFFFFFFFFL) << 37;
	}

	public static long putDummy(GameObject o)
	{
		return objIdNoStore(o.getObjectId());
	}

	/**
	 * генерирует уникальный индентификатор по которому будет ясно что объект вне хранилища но можно будет получить objectId
	 */
	public static long objIdNoStore(int objId)
	{
		return objId & 0xFFFFFFFFL | (STORAGE_NONE & 0x1FL) << 32;
	}

	/**
	 * пересчитывает StoredId, необходимо при изменении ObjectId
	 */
	public static long refreshId(Creature o)
	{
		return o.getObjectId() & 0xFFFFFFFFL | o.getStoredId() >> 32 << 32;
	}

	public static GameObject remove(long storedId)
	{
		int STORAGE_ID = getStorageID(storedId);
		return STORAGE_ID == STORAGE_NONE ? null : storages[STORAGE_ID].remove(getStoredIndex(storedId), getStoredObjectId(storedId));
	}

	private static int getStorageID(long storedId)
	{
		return (int) (storedId >> 32) & 0x1F;
	}

	private static int getStoredIndex(long storedId)
	{
		return (int) (storedId >> 37);
	}

	public static int getStoredObjectId(long storedId)
	{
		return (int) storedId;
	}

	public static StrTable getStats()
	{
		StrTable table = new StrTable("L2 Objects Storage Stats");

		GameObjectArray<?> storage;
		for(int i = 0; i < storages.length; i++)
		{
			if((storage = storages[i]) == null)
				continue;

			synchronized (storage)
			{
				table.set(i, "Name", storage.name);
				table.set(i, "Size / Real", storage.size() + " / " + storage.getRealSize());
				table.set(i, "Capacity / init", storage.capacity() + " / " + storage.initCapacity);
			}
		}

		return table;
	}

	public static List<FakePlayerInstance> getAllFakePlayers()
	{
		return getStorageFakePlayers().getAll();
	}

	/**
	 * использовать только для перебора типа for(L2Player player : getAllFakePlayersForIterate()) ...
	 */
	public static Iterable<FakePlayerInstance> getAllFakePlayersForIterate()
	{
		return getStorageFakePlayers();
	}

	public static FakePlayerInstance getFakePlayerByNpcId(int npcId)
	{
		for(FakePlayerInstance player : getStorageFakePlayers())
		{
			if(npcId == player.getNpcId())
				return player;
		}
		return null;
	}

	public static FakePlayerInstance getFakePlayerById(int npcId)
	{
		for(FakePlayerInstance player : getStorageFakePlayers())
		{
			if(npcId == player.getPlayerId())
				return player;
		}
		return null;
	}

	public static FakePlayerInstance getFakePlayerByName(String name)
	{
		return getStorageFakePlayers().findByName(name);
	}

	public static FakePlayerInstance getFakePlayerByObjId(int objId)
	{
		return getStorageFakePlayers().findByObjectId(objId);
	}
}