/************************************************************************
 * This file is part of GiftPost.									
 *																		
 * GiftPost is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by	
 * the Free Software Foundation, either version 3 of the License, or		
 * (at your option) any later version.									
 *																		
 * GiftPost is distributed in the hope that it will be useful,	
 * but WITHOUT ANY WARRANTY; without even the implied warranty of		
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			
 * GNU General Public License for more details.							
 *																		
 * You should have received a copy of the GNU General Public License
 * along with GiftPost.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************/
package be.Balor.Listeners;

import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

import be.Balor.Workers.TombWorker;
import be.Balor.bukkit.Tomb.Tomb;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class SignListener extends BlockListener {
	private TombWorker worker;

	public SignListener() {
		worker = TombWorker.getInstance();
	}

	@Override
	public void onSignChange(SignChangeEvent e) {
		String line0 = e.getLine(0);
		boolean admin = false;
		if (line0.indexOf("[Tomb]") == 0 && line0.indexOf("]") != -1) {
			if (!e.getLine(1).isEmpty() && worker.hasPerm(e.getPlayer(), "tomb.admin"))
				admin=true;
			
				Tomb tomb = new Tomb((Sign)e.getBlock().getState());
				String deadName;
				if(admin)
				{
					deadName=e.getLine(1);
					tomb.setPlayer(deadName);
				}
				else
				{
					deadName=e.getPlayer().getName();
					tomb.setPlayer(deadName);
				}
				worker.setTomb(deadName, tomb);
			
		}

	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getState() instanceof Sign) {
			Sign sign = (Sign) event.getBlock().getState();
			if (sign.getLine(0).indexOf("[Tomb]") == 0
					&& sign.getLine(0).indexOf("]") != -1
					&& (!worker.hasPerm(event.getPlayer(), "tomb.admin") && !worker
							.getTomb(event.getPlayer().getName()).getSign().equals(sign)))
				event.setCancelled(true);

		}
	}
}
