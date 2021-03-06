package entity.dynamic.mob.work;

import entity.dynamic.item.Item;
import entity.dynamic.mob.Villager;
import entity.nonDynamic.building.container.Container;
import map.Tile;

public class MoveItemJob extends Job {

	private final boolean pickUpJob; // is the job a pickup or drop job
	private Container container;
	private Item material;

	public MoveItemJob(Item material, Villager worker) {
		this(worker, true);
		this.material = material;
		if (this.worker.isHolding(this.material) || !this.material.isReserved(this.worker)) {
			completed = true;
		} else {
			this.material.setReserved(this.worker);
			xloc = material.getTileX();
			yloc = material.getTileY();
			zloc = material.getZ();
		}
	}

	private MoveItemJob(Villager worker, boolean pickUpJob) {
		super(worker);
		this.pickUpJob = pickUpJob;
	}

	public MoveItemJob(int xloc, int yloc, int zloc, Villager worker) {
		this(worker, false);
		this.xloc = xloc; //locations are in tile numbers
		this.yloc = yloc;
		this.zloc = zloc;
	}

	@Override
	protected void start() {
		started = true;
		if (!pickUpJob && (worker.getHolding() == null || (!worker.levels[zloc].isClearTile(xloc, yloc) && !(worker.levels[zloc].getEntityOn(xloc, yloc) instanceof Container)))) {
			completed = true;
			return;
		}
		if (worker.levels[zloc].getEntityOn( xloc, yloc) instanceof Container) {
			container = worker.levels[zloc].getEntityOn(xloc, yloc);
			worker.addJob(new MoveJob(xloc, yloc, zloc, worker, false), 100);
		} else {
			worker.addJob(new MoveJob(xloc, yloc, zloc, worker), 100);
		}
	}

	public void execute() {
		if (!completed && started) {
			if (pickUpJob) {
				if (worker.isHolding(material)) {
					completed = true;
				} else {
					if (container != null) {
						if (worker.aroundTile(material.getTileX(), material.getTileY(), material.getZ())) {
							if (worker.pickUp(material, container)) {
								completed = true;
							}
							return;
						}
					} else if (worker.onSpot(material.getTileX(), material.getTileY(), material.getZ())) {
						if (worker.pickUp(material)) {
							completed = true;
						}
						return;
					}

					if (worker.isMovementNull()) {
						completed = true;
						material.removeReserved();
					}
					worker.move();
				}
			} else {
				if (container != null) {
					if (worker.aroundTile(xloc, yloc, zloc)) {
						worker.drop(container);
						completed = true;
						return;
					}
				} else if (worker.onSpot(xloc, yloc, zloc)) {
					worker.drop();
					completed = true;
					return;
				}
				if (worker.isMovementNull()) {
					completed = true;
				}
				worker.move();
			}
		} else {
			start();
		}
	}

}
