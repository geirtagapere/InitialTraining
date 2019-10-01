package core;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import org.omg.CORBA.PUBLIC_MEMBER;
import org.osbot.iF;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

@ScriptManifest(author = "pAnA", info = "My 3rd script", name = "initialTrainer", version = 0, logo = "")
public class Main extends Script {

	private final int corner1 = 3246;
	private final int corner11 = 3227;
	private final int corner2 = 3264;
	private final int corner22 = 3252;
	private final Area lumbyFrontUpper = new Area(corner1, corner11, corner2, corner22);
	private final int box1 = 3249;
	private final int box11 = 3225;
	private final int box2 = 3266;
	private final int box22 = 3219;
	private final Area lumbyFrontLower = new Area(box1, box11, box2, box22);
	
	@Override
	public void onStart() {
		log("Let's get started!");
	}

	@Override
	public int onLoop() throws InterruptedException {
		
		int myAreaCoinsX = myPosition().getX();
		int myAreaCoinsY = myPosition().getY();
		final int corn1 = myAreaCoinsX-1;
		final int corner11 = myAreaCoinsY-1;
		final int corner2 = myAreaCoinsX+1;
		final int corner22 = myAreaCoinsY+1;
		final Area tilesNextToMe = new Area(corn1, corner11, corner2, corner22);
		final Area lumbyFront = new Area(3217, 3215, 3225, 3221);
		GroundItem coinItem = groundItems.closest("Coins");
		GroundItem groundItem = groundItems.closest("Bones", "Coins", "Hammer",
				"Bronze axe","Air talisman","Bucket","Chef's hat","Beer","Water rune", 
				"Body rune", "Mind rune", "Water rune", "Earth rune", "Bronze arrow", 
				"Brass necklace","Bronze bolts","Bronze sq shield");
		
		if (lumbyFront.contains(myPosition())) {
			walkToTraining();
		} else if (coinItem != null && tilesNextToMe.contains(coinItem) && inventory.getAmount("Coins") < 10) {
			coinItem.interact("Take");
			log("k6rval olevate coinide korjamine");
		} else if (coinItem != null && (lumbyFrontLower.contains(coinItem) || lumbyFrontUpper.contains(coinItem)) && inventory.getAmount("Coins") < 10) {
			log("suva coinide korjamine");
			coinItem.interact("Take");
			getCamera().toEntity(coinItem);
		} else if (!lumbyFrontUpper.contains(myPosition()) && !lumbyFrontLower.contains(myPosition())) {
			walkToTraining();
		} else if (!getEquipment().isWearingItem(EquipmentSlot.WEAPON) && !getEquipment().isWearingItem(EquipmentSlot.SHIELD)) {
			equipItems();
		} else if (getSkills().getStatic(Skill.ATTACK) < 2) {
			killGoblinsMelee();
		} else if (getSkills().getStatic(Skill.MAGIC) < 2) {
			killGoblinsMagic();
		} else if (inventory.getAmount("Coins") < 10) {
			killGoblinsMelee();
		} else if (!getInventory().isFull() && (lumbyFrontLower.contains(groundItem) || lumbyFrontUpper.contains(groundItem))) {
			pickUpItems(groundItem);
		} else if (getSkills().getStatic(Skill.MAGIC) >= 2 && getSkills().getStatic(Skill.ATTACK) >= 2 && inventory.getAmount("Coins") >= 10) {
			log("lets go alkharid");
			goToAlKharid();
		}
		return random(232,429);
	}
	
	public void pickUpItems(GroundItem gItem) {
		log("pickUpItems");
		getTabs().open(Tab.INVENTORY);
		int inventorySize = getInventory().getEmptySlotCount();
		log(inventorySize);

		if (gItem != null) {
			gItem.interact("Take");
			new ConditionalSleep(random(1952,4823), random(379,743)) {
				@Override
				public boolean condition() throws InterruptedException {
					return getInventory().getEmptySlotCount() == getInventory().getEmptySlotCount()-1;
				}
			}.sleep();
		}
	}
	
	public void goToAlKharid() {
		final Area alkharidArea = new Area(3280, 3227, 3315, 3238);
		getWalking().webWalk(alkharidArea);
		getLogoutTab().logOut();
		stop(true);
	}
	
	public void killGoblinsMelee() {
		NPC goblin = getNpcs().closest("Goblin");

		if (goblin != null && !goblin.isUnderAttack() && getMap().canReach(goblin) 
			&& (lumbyFrontLower.contains(goblin) || lumbyFrontUpper.contains(goblin))) {
			goblin.interact("Attack");
			getCamera().toEntity(goblin);
			log("melee goblin");
			new ConditionalSleep(random(53920,84199), random(420,1578)) {
				@Override
				public boolean condition() throws InterruptedException {
					checkForHealth();
					return (goblin.getHealth() == 0);
				}
			}.sleep();
		}
	}
	
	public void killGoblinsMagic() {
		NPC goblin = getNpcs().closest("Goblin");
		getTabs().open(Tab.MAGIC);
		log("goblin NPC ++ " + goblin);
		if (goblin != null && !goblin.isUnderAttack() && getMap().canReach(goblin) && (lumbyFrontLower.contains(goblin) || lumbyFrontUpper.contains(goblin))) {
			
			getCamera().toEntity(goblin);
			log("magic goblin");
			new ConditionalSleep(random(53920,84199), random(433,893)) {
				@Override
				public boolean condition() throws InterruptedException {
					log("mage spelllllllin");
					checkForHealth();
					magic.castSpellOnEntity(Spells.NormalSpells.WIND_STRIKE, goblin);
					return (goblin.getHealth() == 0 || getSkills().getStatic(Skill.MAGIC) >= 2);
				}
			}.sleep();
		} else if (goblin == null) {
			walkToTraining();
		}
	}
	
	public void checkForHealth() {
		log("check for health");
		log(myPlayer().getHealth() + " health");
		int luckyNrForEating = random(1,2);
		if (luckyNrForEating == 1 && myPlayer().getHealth() < 40) {			
			if (getInventory().contains("Bread")) {
				getInventory().interact("Eat", "Bread");
			} else if (getInventory().contains("Shrimps")) {
				getInventory().interact("Eat", "Shrimps");
			}
		} else if (luckyNrForEating == 2 && myPlayer().getHealth() < 40) {
			if (getInventory().contains("Bread")) {
				getInventory().interact("Eat", "Shrimps");
			} else if (getInventory().contains("Shrimps")) {
				getInventory().interact("Eat", "Bread");
			}
		}
	}
	
	public void walkToTraining() {
		int luckyNr = random(1,2);
		
		if (luckyNr == 1) {
			log("Liigun lumby upper");
			getWalking().webWalk(lumbyFrontUpper);
		} else {
			log("Liigun lumby lower");
			getWalking().webWalk(lumbyFrontLower);
		}
		
		
	}

	public void equipItems() {
		log("equipping gear");
		if (!getTabs().isOpen(Tab.INVENTORY)) {
        	log("avan invi");
        	
        	int choice = random(1,4);
        	log(choice);
        	if (choice == 4) {
        		log("hiirega");
            	getTabs().open(Tab.INVENTORY);
        	} else if (choice == 1 || choice == 2 || choice == 3) {
        		log("escapega");
        		getKeyboard().typeKey((char) KeyEvent.VK_ESCAPE);
        	}
        } else {
        	int luckyNr2 = random(1,3);

        	if (luckyNr2 == 1 || luckyNr2 == 2) {
        		log("m66k ja kilp");
				getInventory().interact("Wield", "Bronze sword");
				getInventory().interact("Wield", "Wooden shield");
			} else {
				log("kilp ja m66k");
				getInventory().interact("Wield", "Wooden shield");
				getInventory().interact("Wield", "Bronze sword");
			}
		}
	}

	@Override
	public void onExit() {
		log("Thanks for running my valmistaja!");
	}

	@Override
	public void onPaint(Graphics2D g) {

	}
	
}