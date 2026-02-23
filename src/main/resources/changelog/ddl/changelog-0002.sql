CREATE TABLE public.follows (
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE
);

ALTER TABLE public.follows
    ADD CONSTRAINT pk_follows PRIMARY KEY (follower_id, following_id);

ALTER TABLE public.follows
    ADD CONSTRAINT fk_follows_follower_id FOREIGN KEY (follower_id) REFERENCES public.accounts(id) ON DELETE CASCADE;

ALTER TABLE public.follows
    ADD CONSTRAINT fk_follows_following_id FOREIGN KEY (following_id) REFERENCES public.accounts(id) ON DELETE CASCADE;