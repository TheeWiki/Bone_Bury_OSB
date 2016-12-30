package main.script.Bone_Bury;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

@ScriptManifest(author = "Booleans Yay", info = "Bone burying made ez pz", name = "Bone Bury", version = 1, logo = "https://i.imgur.com/2gcLb2Z.png")

public class BoneBury extends Script {
	
	private int deaths, bonesBurried, inventorys, randomsDismissed;
	private long timeStarting = System.currentTimeMillis();
	int currentBonerCount = inventory.getItem("bones").getAmount();
	
	@Override
	public void onStart() throws InterruptedException {
		deaths = 0;
		bonesBurried = 0;
		randomsDismissed = 0;
		getExperienceTracker().start(Skill.PRAYER);
		timeStarting = System.currentTimeMillis();
		getSettings().setRunning(true);
	}

	private enum BotState {
		LOOTING_BONES, BURY_BONES
	};
	
	private BotState getState() {
		return inventory.isFull() ? BotState.BURY_BONES : BotState.LOOTING_BONES;
	}
	
	private boolean dismissRandom() {
		for (NPC npc : npcs.getAll()) {
			if (npc == null || npc.getInteracting() == null
					|| npc.getInteracting() != myPlayer()) {
				continue;
			}
			if (npc.hasAction("Dismiss")) {
				npc.interact("Dismiss");
				randomsDismissed += 1;
				return true;
			}
		}
		return false;

	}
	private void antiBanMode() throws InterruptedException {
		camera.movePitch(random(0, getState() == BotState.BURY_BONES ? 360 : 0));
		sleep(random(500, 900));
		camera.moveYaw(random(0, getState() == BotState.BURY_BONES ? 360 : 0));
		sleep(random(500, 900));
		
		int randomTabs = random(0, 125);
		if (getState() == BotState.LOOTING_BONES) {
			switch (randomTabs) {
			case 0:
				getTabs().open(Tab.EMOTES);
				break;
			case 1:
				getTabs().open(Tab.SKILLS);
				getSkills().hoverSkill(Skill.PRAYER);
				break;
			case 2:
				getTabs().open(Tab.FRIENDS);
				break;
			case 3:
				getTabs().open(Tab.CLANCHAT);
				break;
			case 4:
				getTabs().open(Tab.EQUIPMENT);
				break;
			case 5:
				getTabs().open(Tab.IGNORES);
				break;
			case 6:
				getTabs().open(Tab.INVENTORY);
				break;
			case 7:
				getTabs().open(Tab.LOGOUT);
				break;
			case 8:
				getTabs().open(Tab.PRAYER);
				break;
			case 9:
				getTabs().open(Tab.SKILLS);
				break;
			case 10:
				getTabs().open(Tab.MAGIC);
				break;
			}
			return;
		}
	}
	
	@Override
	public int onLoop() throws InterruptedException {
		antiBanMode();
		int runRNG = random(5);
		if (runRNG == 1) {
			getSettings().setRunning(getSettings().getRunEnergy() < 25 ? false : true);
		}
		if(dismissRandom()) {
            sleep(random(600, 800));
            while(myPlayer().isMoving()) {
                sleep(600);
            }
            sleep(400);
        }
		if (myPlayer().isUnderAttack())
		{
			getWalking().webWalk(Banks.EDGEVILLE);
			getSettings().setRunning(true);
		}
		if (myPlayer() == null)
		{
			deaths += 1;
			stop(true);
		}
		if (inventory.isFull())
		{
			inventorys += 1;
		}
		switch (getState()) {
		case LOOTING_BONES:			
			if (!myPlayer().isAnimating()) {
				GroundItem bone = groundItems.closest("bones");
				if (bone != null) {
					bone.interact("Take");
					sleep(random(900));
					new ConditionalSleep(2_000) {
						@Override
					    public boolean condition() throws InterruptedException {
					        return inventory.getItem("bones").getAmount() > currentBonerCount;
					    }
					}.sleep();
				}
				
			}
			break;
		case BURY_BONES:
			while (!inventory.isEmpty()) {
				Item bones = inventory.getItem("bones");
				if (bones != null) {
					while(inventory.contains("bones")) {
						 inventory.interact("Bury", "bones");
						 sleep(1000);
					}
					long boneCount = inventory.getAmount("bones");
				new ConditionalSleep(random(5_000)) {
					@Override
					public boolean condition() throws InterruptedException {
						return inventory.getAmount("bones") > boneCount;
					}
					}.sleep();
				}
			}
			break;
		}
		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Script terminated");
	}

	@Override
	public void onPaint(Graphics2D g) {
		drawMouse(g);
		Font font = new Font("TimesRoman", Font.PLAIN, 14);
		g.setFont(font);
		g.setColor(Color.WHITE);
		g.drawString("Bone Bury script created by: Booleans Yay", 5, 40);
		g.drawString("Bone Burying Pro v1.2", 5, 55);
		g.drawString("Bones Collected: " + currentBonerCount, 5, 55);
		g.drawString("Bones Burried: " + bonesBurried, 5, 85);		
		g.drawString("Inventories filled: " + inventorys, 5, 100);
		g.drawString("Player Deaths: " + deaths, 5, 115);
		g.drawString("Prayer Experience Gained: " +  getExperienceTracker().getGainedXP(Skill.PRAYER), 5, 130);
		g.drawString("Prayer Levels Gained: " + getExperienceTracker().getGainedLevels(Skill.PRAYER), 5, 145);
		g.drawString("Experience p/HR: " + getExperienceTracker().getGainedXPPerHour(Skill.PRAYER), 5, 160);
		g.drawString("Next Level up in: " + formatTime(getExperienceTracker().getTimeToLevel(Skill.PRAYER)), 5, 175);
		long runTime = System.currentTimeMillis() - timeStarting;
		g.drawString("Script Runtime: " + formatTime(runTime), 5, 190);
		g.drawString("Random Events Skipped: " + randomsDismissed, 5, 205);
//		if (setState(BotState.LOOTING_BONES) != null)
//		{
//			g.drawString("Bot Status: Looting Bones", 5, 250);
//		} else if(setState(BotState.BURY_BONES) != null){
//			g.drawString("Bot Status: Burying inventory bones", 5, 250);
//		}
		g.drawString("Mouse X/Y: " + mouse.getPosition().x + " " + mouse.getPosition().y, 5, 220);
		g.drawString("Player Running: " + settings.isRunning() + (" (Energy: " + settings.getRunEnergy() +")"), 5, 235);
		
		g.drawString("88888888888", 10, 470);
	}

	private void drawMouse(Graphics g) {
		((Graphics2D) g).setRenderingHints(
				new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		Point p = mouse.getPosition();
		Graphics2D spinG = (Graphics2D) g.create();
		Graphics2D spinGRev = (Graphics2D) g.create();
		spinG.setColor(new Color(255, 255, 255));
		spinGRev.setColor(Color.cyan);
		spinG.rotate(System.currentTimeMillis() % 2000d / 2000d * (360d) * 2 * Math.PI / 180.0, p.x, p.y);
		spinGRev.rotate(System.currentTimeMillis() % 2000d / 2000d * (-360d) * 2 * Math.PI / 180.0, p.x, p.y);

		final int outerSize = 20;
		final int innerSize = 12;

		spinG.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		spinGRev.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		spinG.drawArc(p.x - (outerSize / 2), p.y - (outerSize / 2), outerSize, outerSize, 100, 75);
		spinG.drawArc(p.x - (outerSize / 2), p.y - (outerSize / 2), outerSize, outerSize, -100, 75);
		spinGRev.drawArc(p.x - (innerSize / 2), p.y - (innerSize / 2), innerSize, innerSize, 100, 75);
		spinGRev.drawArc(p.x - (innerSize / 2), p.y - (innerSize / 2), innerSize, innerSize, -100, 75);
	}

	private final String formatTime(final long ms) {
		long s = ms / 1000, m = s / 60, h = m / 60, d = h / 24;
		s %= 60;
		m %= 60;
		h %= 24;
		return d > 0 ? String.format("%02d:%02d:%02d:%02d", d, h, m, s)
				: h > 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
	}
}