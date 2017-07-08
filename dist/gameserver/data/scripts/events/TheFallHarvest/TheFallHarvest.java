package events.TheFallHarvest;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheFallHarvest extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(TheFallHarvest.class);
	private static int EVENT_MANAGER_ID = 31255;
	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();

	private static boolean _active = false;

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			_log.info("Loaded Event: The Fall Harvest [state: activated]");
		}
		else
			_log.info("Loaded Event: The Fall Harvest [state: deactivated]");
	}

	/**
	 * Читает статус эвента из базы.
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive("TheFallHarvest");
	}

	/**
	 * Запускает эвент
	 */
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("TheFallHarvest", true))
		{
			spawnEventManagers();
			System.out.println("Event 'The Fall Harvest' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TheFallHarvest.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'The Fall Harvest' already started.");

		_active = true;

		show("admin/events/events.htm", player);
	}

	/**
	 * Останавливает эвент
	 */
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(SetActive("TheFallHarvest", false))
		{
			unSpawnEventManagers();
			System.out.println("Event 'The Fall Harvest' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TheFallHarvest.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'The Fall Harvest' not started.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	/**
	 * Спавнит эвент менеджеров
	 */
	private void spawnEventManagers()
	{
		final int EVENT_MANAGERS[][] = {
				{ 81921, 148921, -3467, 16384 },
				{ 146405, 28360, -2269, 49648 },
				{ 19319, 144919, -3103, 31135 },
				{ -82805, 149890, -3129, 33202 },
				{ -12347, 122549, -3104, 32603 },
				{ 110642, 220165, -3655, 61898 },
				{ 116619, 75463, -2721, 20881 },
				{ 85513, 16014, -3668, 23681 },
				{ 81999, 53793, -1496, 61621 },
				{ 148159, -55484, -2734, 44315 },
				{ 44185, -48502, -797, 27479 },
				{ 86899, -143229, -1293, 22021 } };

		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
	}

	/**
	 * Удаляет спавн эвент менеджеров
	 */
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	@Override
	public void onReload()
	{
		unSpawnEventManagers();
	}

	@Override
	public void onShutdown()
	{

	}

	/**
	 * Обработчик смерти мобов, управляющий эвентовым дропом
	 */
	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		if(_active && SimpleCheckDrop(cha, killer) && Rnd.chance(Config.EVENT_TFH_POLLEN_CHANCE * killer.getPlayer().getRateItems() * ((NpcInstance) cha).getTemplate().rateHp))
			((NpcInstance) cha).dropItem(killer.getPlayer(), 6391, 1);
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.TheFallHarvest.AnnounceEventStarted", null);
	}
}