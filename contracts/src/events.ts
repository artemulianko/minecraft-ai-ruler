/**
 * Represents the base structure of an event in the system.
 * This interface serves as a foundation for more specific event types.
 *
 * @interface BaseEvent
 *
 * @property {string} playerId
 * Unique identifier for the player associated with the event.
 *
 * @property {number} [timestamp]
 * Optional timestamp indicating when the event occurred, represented in milliseconds since the Unix epoch.
 */
interface BaseEvent {
    playerId: string;
    timestamp?: number;
}

/**
 * Represents a position in a 3D space.
 *
 * This interface defines coordinates for a point in a 3-dimensional Cartesian system,
 * using x, y, and z as the axes.
 *
 * Properties:
 * - x: The position along the horizontal axis.
 * - y: The position along the vertical axis.
 * - z: The position along the depth axis.
 */
interface Position {
    x: number;
    y: number;
    z: number;
}

/**
 * Represents an event indicating that a message has been posted.
 * Extends the BaseEvent interface to include additional details specific to message events.
 *
 * @interface MessagePosted
 * @extends BaseEvent
 *
 * @property {string} content - The content of the posted message.
 */
export interface MessagePosted extends BaseEvent{
    content: string;
}

/**
 * Represents an event triggered when a block is destroyed in the game.
 *
 * This event is used to capture information about the destruction of a block,
 * including its location (`position`) and optionally the type of block (`blockType`).
 * It extends from the `BaseEvent` to inherit common event properties.
 *
 * Properties:
 * - `position`: The location of the block that was destroyed.
 * - `blockType` (Optional): The type of block that was destroyed, if applicable.
 */
export interface BlockDestroyed extends BaseEvent {
    position: Position;
    blockType?: string;
}

/**
 * Represents an event that occurs when a player takes damage in the game.
 *
 * This event contains information about the amount of damage taken and the source
 * responsible for inflicting the damage. It extends the BaseEvent interface
 * and adds details specific to damage-related interactions.
 *
 * This interface is typically used within the game to track or respond to
 * occurrences of player damage, enabling game logic, analytics, or visual
 * feedback to be implemented based on the damage event details.
 *
 * Properties:
 * - `damageAmount`: The numeric value representing the amount of damage caused to the player.
 * - `damageSource`: A string describing the entity, object, or mechanism that caused the damage.
 */
export interface PlayerDamaged extends BaseEvent {
    damageAmount: number;
    damageSource: string;
}
