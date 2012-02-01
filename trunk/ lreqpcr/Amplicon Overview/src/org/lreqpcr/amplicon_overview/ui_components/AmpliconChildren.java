/*
 * Copyright (C) 2010  Bob Rutledge
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * and open the template in the editor.
 */
package org.lreqpcr.amplicon_overview.ui_components;

import java.text.DecimalFormat;
import java.util.List;
import org.lreqpcr.core.data_objects.Amplicon;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.openide.nodes.FilterNode.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Bob Rutledge
 */
public class AmpliconChildren extends Children.Keys<Amplicon> {

    private final DatabaseServices db;
    private DecimalFormat df = new DecimalFormat("#0.0");

    public AmpliconChildren(DatabaseServices db, List<Amplicon> ampliconList) {
        this.db = db;
        setKeys(ampliconList);
    }

    @Override
    protected Node[] createNodes(Amplicon amplicon) {
        AmpliconNode node = new AmpliconNode(Children.LEAF, Lookups.singleton(amplicon), db);
        String s = String.valueOf(amplicon.getEmaxAverage());
        if (s.contentEquals("NaN")) {
            node.setName(amplicon.getName() + "  (Av. Emax= n.d." + ")");
            node.setShortDescription("An average Emax could not be determined");
            return new Node[]{node};
        }
        if (amplicon.getEmaxCV() == 0) {
            node.setName(amplicon.getName() + "  (Av. Emax= " + df.format(amplicon.getEmaxAverage() * 100) + "%)");
        } else {
            node.setName(amplicon.getName() + "  (Av. Emax= " + df.format(amplicon.getEmaxAverage() * 100)
                    + " +/-" + df.format(amplicon.getEmaxCV() * 100) + "%)");
        }
        return new Node[]{node};
    }
}
