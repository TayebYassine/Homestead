package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Typed accessor for the active {@code languages/<locale>.yml} file.
 */
public final class LanguageFile extends ResourceFile {

    public LanguageFile(File file) throws FileNotFoundException {
        super(file);
    }

    @Override
    public String getString(String path) {
        return getString(path, "NULL @ " + path);
    }

    /**
     * The chat prefix prepended to every plugin message.
     *
     * @return the prefix string
     */
    public String getPrefix() {
        return getString("prefix");
    }

    /**
     * The default region description used when creating a new region.
     *
     * @return the default region description
     */
    public String getDefaultRegionDescription() {
        return getString("common.default.region-description");
    }

    /**
     * The default war description used when a war is declared.
     *
     * @return the default war description
     */
    public String getDefaultWarDescription() {
        return getString("common.default.war-description");
    }

    /**
     * The default ban reason shown when no reason is provided.
     *
     * @return the default ban reason
     */
    public String getDefaultBanReason() {
        return getString("common.default.reason");
    }

    /**
     * The list of messages broadcast when a war is declared.
     *
     * @return war declaration message lines, never {@code null}
     */
    public List<String> getWarDeclarationMessages() {
        return getStringList("common.war_declaration");
    }

    /**
     * The star character used for level display.
     *
     * @return star character string
     */
    public String getVariableStar() {
        return getString("common.variables.star");
    }

    /**
     * The star colour for a specific rating level.
     *
     * @param rate the rating level (e.g. {@code 1}–{@code 5})
     * @return the colour/formatting code
     */
    public String getVariableStarColor(int rate) {
        return getString("common.variables.star-color." + rate);
    }

    /**
     * The text shown when a value is not applicable.
     *
     * @return "N/A" style text
     */
    public String getVariableNone() {
        return getString("common.variables.none");
    }

    /**
     * The text shown when a value is not available.
     *
     * @return "N/A" style text
     */
    public String getVariableNa() {
        return getString("common.variables.na");
    }

    /**
     * The text shown for a permanent (non-expiring) duration.
     *
     * @return "Permanent" style text
     */
    public String getVariablePermanent() {
        return getString("common.variables.permanent");
    }

    /**
     * The text shown for a "never" timestamp.
     *
     * @return "Never" style text
     */
    public String getVariableNever() {
        return getString("common.variables.never");
    }

    /**
     * Boolean display text for {@code true}.
     *
     * @return "Yes" / "True" style text
     */
    public String getVariableIsTrue() {
        return getString("common.variables.isTrue");
    }

    /**
     * Boolean display text for {@code false}.
     *
     * @return "No" / "False" style text
     */
    public String getVariableIsFalse() {
        return getString("common.variables.isFalse");
    }

    /**
     * Enabled-state display text.
     *
     * @return "Enabled" style text
     */
    public String getVariableIsEnabled() {
        return getString("common.variables.isEnabled");
    }

    /**
     * Disabled-state display text.
     *
     * @return "Disabled" style text
     */
    public String getVariableIsDisabled() {
        return getString("common.variables.isDisabled");
    }

    /**
     * Text shown when a flag is set.
     *
     * @return flag-set text
     */
    public String getVariableFlagSet() {
        return getString("common.variables.flagSet");
    }

    /**
     * Text shown when a flag is unset.
     *
     * @return flag-unset text
     */
    public String getVariableFlagUnset() {
        return getString("common.variables.flagUnset");
    }

    /**
     * The text shown for a banned player status.
     *
     * @return "Banned" style text
     */
    public String getVariableBanned() {
        return getString("common.variables.banned");
    }

    /**
     * The text shown for an online player status.
     *
     * @return "Online" style text
     */
    public String getVariableOnline() {
        return getString("common.variables.online");
    }

    /**
     * The text shown for an offline player status.
     *
     * @return "Offline" style text
     */
    public String getVariableOffline() {
        return getString("common.variables.offline");
    }

    /**
     * A common variable by its key name.
     *
     * @param name the variable key under {@code common.variables}
     * @return the variable text
     */
    public String getCommonVariable(String name) {
        return getString("common.variables." + name);
    }

    /**
     * The format string for displaying a {@link org.bukkit.Location}.
     *
     * @return location format pattern
     */
    public String getFormatterLocation() {
        return getString("formatters.location");
    }

    /**
     * The format string for displaying a {@link org.bukkit.Chunk}.
     *
     * @return chunk format pattern
     */
    public String getFormatterChunk() {
        return getString("formatters.chunk");
    }

    /**
     * The format string for displaying a currency balance.
     *
     * @return balance format pattern
     */
    public String getFormatterBalance() {
        return getString("formatters.balance");
    }

    /**
     * The date / time pattern used by {@link java.text.SimpleDateFormat}.
     *
     * @return date format pattern
     */
    public String getFormatterDateFormat() {
        return getString("formatters.date-format");
    }

    /**
     * The format string for displaying a timestamp with "ago" suffix.
     *
     * @return date-ago format pattern
     */
    public String getFormatterDate() {
        return getString("formatters.date");
    }

    /**
     * The format string for displaying a human-readable duration.
     *
     * @return duration format pattern
     */
    public String getFormatterDuration() {
        return getString("formatters.duration");
    }

    /**
     * The format string for GUI pagination titles.
     *
     * @return pagination title format
     */
    public String getFormatterGuiPaginationTitle() {
        return getString("formatters.gui-pagination-title");
    }

    /**
     * The format string for private / party chat messages.
     *
     * @return private chat format pattern
     */
    public String getFormatterPrivateChat() {
        return getString("formatters.private-chat");
    }

    /**
     * The format string for a list of player regions.
     *
     * @return player regions format
     */
    public String getFormatterPlayerRegions() {
        return getString("formatters.player-regions");
    }

    /**
     * The joining string between player region names.
     *
     * @return region list separator
     */
    public String getFormatterPlayerRegionsJoining() {
        return getString("formatters.player-regions-joining");
    }

    /**
     * The format string for a list of region members.
     *
     * @return region members format
     */
    public String getFormatterRegionMembers() {
        return getString("formatters.region-members");
    }

    /**
     * The joining string between region member names.
     *
     * @return member list separator
     */
    public String getFormatterRegionMembersJoining() {
        return getString("formatters.region-members-joining");
    }

    /**
     * The format string for a list of war participant regions.
     *
     * @return war regions format
     */
    public String getFormatterWarRegions() {
        return getString("formatters.war-regions");
    }

    /**
     * The joining string between war region names.
     *
     * @return war region list separator
     */
    public String getFormatterWarRegionsJoining() {
        return getString("formatters.war-regions-joining");
    }

    /**
     * The "ago" label for days.
     *
     * @return days-ago label
     */
    public String getFormatterAgoDays() {
        return getString("formatters.ago-days");
    }

    /**
     * The "ago" label for hours.
     *
     * @return hours-ago label
     */
    public String getFormatterAgoHours() {
        return getString("formatters.ago-hours");
    }

    /**
     * The "ago" label for minutes.
     *
     * @return minutes-ago label
     */
    public String getFormatterAgoMinutes() {
        return getString("formatters.ago-minutes");
    }

    /**
     * The "ago" label for seconds.
     *
     * @return seconds-ago label
     */
    public String getFormatterAgoSeconds() {
        return getString("formatters.ago-seconds");
    }

    /**
     * All keys registered under the help command descriptions section.
     *
     * @return list of command keys
     */
    public List<String> getHelpCommandDescriptionKeys() {
        return getKeysUnderPath("command-descriptions");
    }

    /**
     * The description text for a specific help command.
     *
     * @param key the command key
     * @return the description text
     */
    public String getHelpCommandDescription(String key) {
        return getString("command-descriptions." + key);
    }

    /**
     * The format for a single help page entry.
     *
     * @return entry format string
     */
    public String getHelpEntryFormat() {
        return getString("commands.help.entry-format");
    }

    /**
     * The hover text for the "previous page" button.
     *
     * @return previous-page hover text
     */
    public String getHelpPrevHover() {
        return getString("commands.help.prev-hover");
    }

    /**
     * The label for the "previous page" button.
     *
     * @return previous-page label
     */
    public String getHelpPrev() {
        return getString("commands.help.prev");
    }

    /**
     * The label for a disabled "previous page" button.
     *
     * @return disabled previous-page label
     */
    public String getHelpPrevDisabled() {
        return getString("commands.help.prev-disabled");
    }

    /**
     * The hover text for the "next page" button.
     *
     * @return next-page hover text
     */
    public String getHelpNextHover() {
        return getString("commands.help.next-hover");
    }

    /**
     * The label for the "next page" button.
     *
     * @return next-page label
     */
    public String getHelpNext() {
        return getString("commands.help.next");
    }

    /**
     * The label for a disabled "next page" button.
     *
     * @return disabled next-page label
     */
    public String getHelpNextDisabled() {
        return getString("commands.help.next-disabled");
    }

    /**
     * The footer text shown at the bottom of every help page.
     *
     * @return help footer text
     */
    public String getHelpFooter() {
        return getString("commands.help.footer");
    }

    /**
     * The title lines shown for a specific input prompt.
     *
     * @param promptId the prompt identifier
     * @return title lines, never {@code null}
     */
    public List<String> getInputPromptTitle(String promptId) {
        return getStringList("input." + promptId + ".title");
    }

    /**
     * The chat message for a specific input prompt.
     *
     * @param promptId the prompt identifier
     * @return chat prompt text
     */
    public String getInputPromptChat(String promptId) {
        return getString("input." + promptId + ".chat");
    }

    /**
     * The action-bar message for a specific input prompt.
     *
     * @param promptId the prompt identifier
     * @return action-bar text
     */
    public String getInputPromptActionbar(String promptId) {
        return getString("input." + promptId + ".actionbar");
    }

    /**
     * The log message template for a specific log type.
     *
     * @param logId the log identifier
     * @return the log message template
     */
    public String getLogMessage(String logId) {
        return getString("logs." + logId);
    }

    /**
     * The variable placeholder text for the log author.
     *
     * @return log-author variable text
     */
    public String getVariableLogAuthor() {
        return getString("common.variables.log-author");
    }

    /**
     * The variable text for the rent vacate notice period.
     *
     * @return rent-vacate-notice variable text
     */
    public String getVariableRentVacateNotice() {
        return getString("common.variables.rent-vacate-notice");
    }
}
