package services;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;

public class ManaRegen extends Functions
{
	private static final int ADENA = 57;
	private static final long PRICE = 5; //5 аден за 1 МП

	public void DoManaRegen()
	{
		Player player = getSelf();
		long mp = (long) Math.floor(player.getMaxMp() - player.getCurrentMp());
		long fullCost = mp * PRICE;
		if(fullCost <= 0)
		{
			player.sendPacket(SystemMsg.NOTHING_HAPPENED);
			return;
		}
		if(getItemCount(player, ADENA) >= fullCost)
		{
			removeItem(player, ADENA, fullCost);
			player.sendPacket(new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber(mp));
			player.setCurrentMp(player.getMaxMp());
		}
		else
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
	}
}