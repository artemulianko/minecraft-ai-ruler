interface BaseAction {
    // Base action type
}

/**
 * Mute player action.
 * timeout - value in ms represents how long a player lasts muted, null timout - permanent mute
 */
export interface MutePlayer extends BaseAction {
    readonly actionType: 'MutePlayer';
    playerId: string;
    mute: boolean;
    reason?: string;
    timeout?: number; // ms, null - permanent
}

export interface SendMessage extends BaseAction {
    readonly actionType: 'SendMessage';
    content: string;
}