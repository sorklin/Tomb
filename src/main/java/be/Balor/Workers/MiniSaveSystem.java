/*This file is part of Tomb.

    Tomb is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Tomb is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Tomb.  If not, see <http://www.gnu.org/licenses/>.*/
package be.Balor.Workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import be.Balor.bukkit.Tomb.Tomb;
import be.Balor.bukkit.Tomb.TombPlugin;
import be.Balor.bukkit.Tomb.TombSave;
import com.mini.Arguments;
import com.mini.Mini;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class MiniSaveSystem {
	String path;
        private Mini tombDB;
        

	public MiniSaveSystem(String path) {
                try {
                    this.path = path;
                    File dir = new File(path);
                    if (!dir.exists())
                            dir.mkdir();
                
                    dir = new File(this.path + File.separator + "tombs.mini");
                    if(!dir.exists())
                        dir.createNewFile();

                    tombDB = new Mini(dir.getParent(), dir.getName());
                } catch (IOException ioe) {
//                    TombPlugin.slog("Could not find/create/connect to tomb.mini");
                }
	}

	/**
	 * Save all the tombs to the file tombs.dat
	 * 
	 * @param toBeSaved
	 */
	public void save(HashMap<String, Tomb> toBeSaved) {

                if(tombDB == null){
//                    TombPlugin.slog("Error: db is null.");
                    return;
                }
                
		for (String name : toBeSaved.keySet()){
                    Tomb t = toBeSaved.get(name);
                    Arguments entry = new Arguments(name);
                    
                    entry.setValue("player", t.getPlayer());
                    entry.setValue("deaths", String.valueOf(t.getDeaths()));
                    entry.setValue("reason", t.getReason());
                    entry.setValue("deathloc", 
                            (t.getDeathLoc() != null) ? t.getDeathLoc().toString() : "null");
                    
                    tombDB.addIndex(entry.getKey(), entry);
                    tombDB.update();
                }
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Tomb> load() {
		HashMap<String, Tomb> result = new HashMap<String, Tomb>();
		HashMap<String, TombSave> saved=null;
		File saveFile = new File(this.path + File.separator + "tombs.mini");
		if (!saveFile.exists())
			return new HashMap<String, Tomb>();

		FileInputStream fis = null;
		ObjectInputStream in = null;

		try {
			fis = new FileInputStream(saveFile);
			in = new ObjectInputStream(fis);
			saved = (HashMap<String, TombSave>) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		if(saved == null)
			return new HashMap<String, Tomb>();
		for(String name :  saved.keySet())
			result.put(name, saved.get(name).load());
		
		return result;

	}
        
        private Location locationFromString(String data) throws NullPointerException {
            //World: (?<=name=)\w+
            //Coords: (?<==)-?\d+\.\d+  (returns 5 matches (x, y, z, yaw, pitch).

            //NPE if the world is NULL i.e., if MV or other multiverse plugin not loaded.

            String world = "";
            List<String> coords = new ArrayList<String>();
            Location result = null;

            Pattern p = Pattern.compile("(?<=name=)\\w+");
            Matcher m = p.matcher(data);
            if(m.find()){
                world = m.group();
            }

            p = Pattern.compile("(?<==)-?\\d+\\.\\d+");
            m = p.matcher(data);
            if(m != null) {
                while(m.find()) {
                    coords.add(m.group());
                }
            }

            if(!world.isEmpty() && coords.size() == 5) {
                //Valid pull data.
                result = new Location(TombPlugin.getBukkitServer().getWorld(world), 
                        Double.valueOf(coords.get(0)), 
                        Double.valueOf(coords.get(1)), 
                        Double.valueOf(coords.get(2)));
            }
            return result;
        }

}
