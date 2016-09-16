package me.gberg.matterdroid.adapters.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.model.Team;

/**
 * Created by gberg on 31/07/16.
 */
public class ChooseTeamTeamItem extends AbstractItem<ChooseTeamTeamItem, ChooseTeamTeamItem.ViewHolder> {

    private final Team team;

    public ChooseTeamTeamItem(final Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public int getType() {
        return R.id.id_it_choose_team_team;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.it_choose_team_team;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.name.setText(team.displayName);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.it_choose_team_team_name)
        TextView name;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private static final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();

    protected static class ItemFactory implements ViewHolderFactory<ViewHolder> {
        public ViewHolder create(View v) {
            return new ViewHolder(v);
        }
    }

    @Override
    public ViewHolderFactory<? extends ViewHolder> getFactory() {
        return FACTORY;
    }
}
