package l2s.gameserver.network.l2.c2s;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.SkillEnchantInfoHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillInfoPacket;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillResult;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.SkillEnchantInfo;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.SkillUtils;

/**
 * Format chdd
 * c: (id) 0xD0
 * h: (subid) 0x0F
 * d: skill id
 * d: skill lvl
 */
public class RequestExEnchantSkill extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isTransformed() || activeChar.isMounted() || activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_YOU_CAN_USE_THE_SKILL_ENHANCING_FUNCTION_UNDER_OFFBATTLE_STATUS_AND_CANNOT_USE_THE_FUNCTION_WHILE_TRANSFORMING_BATTLING_AND_ONBOARD);
			return;
		}

		if(activeChar.getLevel() < 85)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_ON_THIS_LEVEL_YOU_CAN_USE_THE_CORRESPONDING_FUNCTION_ON_LEVELS_HIGHER_THAN_76LV_);
			return;
		}

		if(!activeChar.getClassId().isAwaked())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_YOU_CAN_USE_CORRESPONDING_FUNCTION_WHEN_COMPLETING_THE_THIRD_CLASS_CHANGE);
			return;
		}

		Skill skill = activeChar.getKnownSkill(_skillId);
		if(skill != null && activeChar.isSkillDisabled(skill))
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_YOU_CAN_USE_THE_SKILL_ENHANCING_FUNCTION_UNDER_OFFBATTLE_STATUS_AND_CANNOT_USE_THE_FUNCTION_WHILE_TRANSFORMING_BATTLING_AND_ONBOARD);
			return;
		}

		skill = SkillHolder.getInstance().getSkill(_skillId, _skillLvl);
		if(skill == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!SkillUtils.isAvailableSkillEnchant(activeChar, skill, Skill.EnchantType.NORMAL))
		{
			activeChar.sendActionFailed();
			return;
		}

		final int enchantLevel = SkillUtils.getSkillEnchantLevel(skill.getLevel());
		if(enchantLevel <= 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		final SkillEnchantInfo info = SkillEnchantInfoHolder.getInstance().getInfo(enchantLevel);
		if(info == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		final long requiredSp = info.getSp();
		final long requiredAdena = info.getAdena();
		final int rate = info.getSuccesRate();
		final int requiredItemId = info.getNormalEnchantItemId();

		if(activeChar.getSp() < requiredSp)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			return;
		}

		if(activeChar.getAdena() < requiredAdena)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(requiredItemId > 0)
		{
			if(Functions.removeItem(activeChar, requiredItemId, 1) != 1)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT));
				return;
			}
		}

		if(Rnd.chance(rate))
		{
			activeChar.addExpAndSp(0, -1 * requiredSp);
			Functions.removeItem(activeChar, 57, requiredAdena);
			activeChar.sendPacket(new SystemMessage(SystemMessage.SP_HAS_DECREASED_BY_S1).addNumber(requiredSp), new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(skill.getId(), skill.getLevel()), new ExEnchantSkillResult(1));
			Log.add(activeChar.getName() + "|Successfully enchanted|" + skill.getId() + "|to+" + enchantLevel + "|" + rate, "enchant_skills");
		}
		else
		{
			skill = SkillHolder.getInstance().getSkill(_skillId, SkillUtils.getSkillLevelFromMask(skill.getLevel()));
			activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_IN_ENCHANTING_SKILL_S1).addSkillName(skill.getId(), skill.getLevel()), new ExEnchantSkillResult(0));
			Log.add(activeChar.getName() + "|Failed to enchant|" + skill.getId() + "|to+" + enchantLevel + "|" + rate, "enchant_skills");
		}

		activeChar.addSkill(skill, true);
		activeChar.sendSkillList();
		activeChar.updateSkillShortcuts(skill.getId(), skill.getLevel());
		activeChar.sendPacket(new ExEnchantSkillInfoPacket(skill.getId(), skill.getLevel()));
	}
}